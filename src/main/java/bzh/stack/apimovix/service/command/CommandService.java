package bzh.stack.apimovix.service.command;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.anomalie.AnomalieCreateDTO;
import bzh.stack.apimovix.dto.command.CommandExpeditionDTO;
import bzh.stack.apimovix.dto.command.CommandSearchDTO;
import bzh.stack.apimovix.dto.command.CommandSearchResponseDTO;
import bzh.stack.apimovix.dto.command.CommandUpdateDTO;
import bzh.stack.apimovix.dto.command.CommandUpdateStatusDTO;
import bzh.stack.apimovix.dto.command.CommandUpdateTarifDTO;
import bzh.stack.apimovix.dto.importer.CommandImporterDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.model.History.HistoryCommandStatus;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;
import bzh.stack.apimovix.model.Picture.CommandPicture;
import bzh.stack.apimovix.model.StatusType.CommandStatus;
import bzh.stack.apimovix.model.StatusType.PackageStatus;
import bzh.stack.apimovix.repository.command.CommandPictureRepository;
import bzh.stack.apimovix.repository.command.CommandRepository;
import bzh.stack.apimovix.service.AnomalieService;
import bzh.stack.apimovix.service.packageservices.HistoryPackageStatusService;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.service.packageservices.PackageStatusService;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
import bzh.stack.apimovix.service.picture.PictureService;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository commandRepository;
    private final CommandPictureRepository commandPictureRepository;
    private final CommandStatusService commandStatusService;
    private final PictureService pictureService;
    private final PharmacyService pharmacyService;
    private final PackageService packageService;
    private final HistoryCommandStatusService historyCommandStatusService;
    private final PackageStatusService packageStatusService;
    private final HistoryPackageStatusService historyPackageStatusService;
    private final AnomalieService anomalieService;

    @Transactional(readOnly = true)
    public Optional<Command> findPharmacyCommandByDate(Account account, String cip, LocalDateTime date) {
        Command command = commandRepository.findPharmacyCommandByDate(account, cip, date);
        return Optional.ofNullable(command);
    }

    @Transactional
    public Command save(Command command) {
        return commandRepository.save(command);
    }

    @Transactional(readOnly = true)
    public List<Command> findCommandsByIds(Account account, List<UUID> ids) {
        return commandRepository.findCommandsByIds(account, ids);
    }

    @Transactional
    public Optional<Command> createCommand(String cip, Sender sender, Profil profil,
            @Valid CommandImporterDTO commandDTO, LocalDateTime expDate, @NotNull Boolean newPharmacy) {
        Optional<Pharmacy> pharmacyOptional = pharmacyService.findPharmacyByAccount(sender.getAccount(), cip);
        if (pharmacyOptional.isEmpty())
            return Optional.empty();
        Command command = createCommand(pharmacyOptional.get(), sender, profil, commandDTO, expDate, newPharmacy);
        List<PackageEntity> packages = commandDTO.getPackages().stream()
                .map(packageDTO -> {
                    PackageEntity newPackage = packageService.createPackage(
                            command,
                            packageDTO,
                            commandDTO.getNum_transport());
                    packageDTO.setBarcode(newPackage.getBarcode());
                    packageDTO.setCNumTransport(newPackage.getCNumTransport());
                    return newPackage;
                })
                .collect(Collectors.toList());
                System.out.println(packages.size());
        command.setPackages(packages);
        return Optional.ofNullable(commandRepository.save(command));
    }

    @Transactional
    public Command createCommand(Pharmacy pharmacy, Sender sender, Profil profil, @Valid CommandImporterDTO commandDTO,
            LocalDateTime expDate, @NotNull Boolean newPharmacy) {
        if (sender == null || sender.getCode() == null)
            return null;

        Command command = new Command();

        if (commandDTO != null) {
            command.mapFromDTO(commandDTO);
        }
        command.setExpDate(expDate);
        command.setSender(sender);
        command.setPharmacy(pharmacy);
        command.setId(UUID.randomUUID());
        command.setNewPharmacy(newPharmacy);
        

        command = commandRepository.save(command);

        Optional<CommandStatus> commandStatus = commandStatusService.findCommandStatus(1);
        HistoryCommandStatus status = historyCommandStatusService.createHistoryCommandStatus(command, profil,
                commandStatus.get());

        command.setLastHistoryStatus(status);
        return commandRepository.save(command);
    }

    /**
     * Mappe un statut de commande vers le statut de package correspondant
     * @param commandStatusId L'ID du statut de commande
     * @return L'ID du statut de package correspondant, ou null si aucun mapping n'existe
     */
    private Integer mapCommandStatusToPackageStatus(Integer commandStatusId) {
        switch (commandStatusId) {
            case 1: // "À enlever"
                return 1;
            case 2: // "Chargé"
                return 2;
            case 3: // "Livré"
                return 3;
            case 4: // "Non Livré"
                return 4;
            case 5: // "Livré incomplet"
                return 3; // Même statut que "Livré" pour les packages
            case 6: // "Chargé incomplet"
                return 2; // Même statut que "Chargé" pour les packages
            case 7: // "Non chargé - MANQUANT"
                return 5;
            case 8: // "Non livré - Inaccessible"
                return 4; // Même statut que "Non Livré" pour les packages
            case 9: // "Non livré - Instructions invalides"
                return 4; // Même statut que "Non Livré" pour les packages
            default:
                return null;
        }
    }

    /**
     * Crée automatiquement une anomalie si le statut de commande le nécessite
     * @param profil Le profil qui effectue l'action
     * @param command La commande concernée
     * @param status Le statut de commande appliqué
     * @param isWeb Si true, ne génère pas d'anomalie
     * @param comment Commentaire à ajouter à l'anomalie
     */
    private void createAnomalieIfNeeded(Profil profil, Command command, CommandStatus status, Boolean isWeb, String comment) {
        if (isWeb != null && isWeb) {
            return;
        }
        
        Integer statusId = status.getId();
        boolean shouldCreateAnomalie = statusId == 4 || 
                                       statusId == 5 || 
                                       statusId == 6 || 
                                       statusId == 7 || 
                                       statusId == 8 || 
                                       statusId == 9;
        if (!shouldCreateAnomalie) {
            return;
        }
        
        if (command.getPharmacy() != null) {
            try {
                AnomalieCreateDTO anomalieCreateDTO = new AnomalieCreateDTO();
                anomalieCreateDTO.setCip(command.getPharmacy().getCip());
                
                String code = "other";
                String actions = "Retour à l'expéditeur";
                
                anomalieCreateDTO.setCode(code);
                anomalieCreateDTO.setOther(status.getName() + ". " + (comment != null ? comment : "Aucun commentaire ajouté"));
                anomalieCreateDTO.setActions(actions);
                
                if (command.getPackages() != null && !command.getPackages().isEmpty()) {
                    List<String> barcodes = command.getPackages().stream()
                            .map(PackageEntity::getBarcode)
                            .collect(Collectors.toList());
                    anomalieCreateDTO.setBarcodes(barcodes);
                }
                
                anomalieService.createAnomalie(profil, anomalieCreateDTO);
            } catch (Exception e) {
                System.err.println("Erreur lors de la création automatique de l'anomalie: " + e.getMessage());
            }
        }
    }

    @Transactional
    public Command updateCommandStatus(Profil profil, CommandStatus status, Command command) {
        return updateCommandStatus(profil, status, command, null, null);
    }

    @Transactional
    public Command updateCommandStatus(Profil profil, CommandStatus status, Command command, Boolean isWeb, String comment) {
        HistoryCommandStatus historyCommandStatus = historyCommandStatusService.createHistoryCommandStatus(command,
                profil, status);
        command.setLastHistoryStatus(historyCommandStatus);
        
        Integer packageStatusId = mapCommandStatusToPackageStatus(status.getId());
        if (isWeb && packageStatusId != null && command.getPackages() != null) {
            Optional<PackageStatus> packageStatus = packageStatusService.findPackageStatus(packageStatusId);
            if (packageStatus.isPresent()) {
                for (PackageEntity packageEntity : command.getPackages()) {
                    packageService.updatePackageStatus(profil, packageStatus.get(), packageEntity);
                }
            }
        }
        
        createAnomalieIfNeeded(profil, command, status, isWeb, comment);
        
        return commandRepository.save(command);
    }

    @Transactional
    public boolean updateCommandStatusBulk(Profil profil, @Valid CommandUpdateStatusDTO commandUpdateStatusDTO) {
        Optional<CommandStatus> optStatus = commandStatusService
                .findCommandStatus(commandUpdateStatusDTO.getStatusId());
        if (optStatus.isEmpty()) {
            return false;
        }
        CommandStatus status = optStatus.get();

        List<Command> commandsToUpdate = new ArrayList<>();
        List<PackageEntity> packagesToUpdate = new ArrayList<>();

        Integer packageStatusId = mapCommandStatusToPackageStatus(status.getId());
        Optional<PackageStatus> packageStatus = Optional.empty();
        if (packageStatusId != null) {
            packageStatus = packageStatusService.findPackageStatus(packageStatusId);
        }

        for (String commandId : commandUpdateStatusDTO.getCommandIds()) {
            UUID uuid = UUID.fromString(commandId);
            System.out.println(uuid);
            Optional<Command> optCommand = findById(profil.getAccount(), uuid);
            if (optCommand.isEmpty()) {
                return false;
            }

            Command command = optCommand.get();
            HistoryCommandStatus historyCommandStatus = historyCommandStatusService.createHistoryCommandStatus(command,
                    profil, status, commandUpdateStatusDTO.getCreatedAt());
            command.setLastHistoryStatus(historyCommandStatus);

            if (commandUpdateStatusDTO.getLatitude() != null && commandUpdateStatusDTO.getLatitude() != 0) {
                command.setLatitude(commandUpdateStatusDTO.getLatitude());
            }
            if (commandUpdateStatusDTO.getLongitude() != null && commandUpdateStatusDTO.getLongitude() != 0) {
                command.setLongitude(commandUpdateStatusDTO.getLongitude());
            }

            commandsToUpdate.add(command);
            
            if (commandUpdateStatusDTO.getIsWeb() && packageStatus.isPresent() && command.getPackages() != null) {
                for (PackageEntity packageEntity : command.getPackages()) {
                    // Vérifier si le statut actuel du package est différent du nouveau statut
                    boolean packageStatusDifferent = packageEntity.getLastHistoryStatus() == null || 
                                                     packageEntity.getLastHistoryStatus().getStatus() == null ||
                                                     !packageEntity.getLastHistoryStatus().getStatus().getId().equals(packageStatus.get().getId());
                    
                    // Ne mettre à jour que si le statut est différent
                    if (packageStatusDifferent) {
                        HistoryPackageStatus historyPackageStatus = historyPackageStatusService.createHistoryPackageStatus(packageEntity, profil, packageStatus.get());
                        packageEntity.setLastHistoryStatus(historyPackageStatus);
                        packagesToUpdate.add(packageEntity);
                    }
                }
            }
            
            // Créer une anomalie automatique si nécessaire (statuts 4, 7, 8, 9)
            createAnomalieIfNeeded(profil, command, status, commandUpdateStatusDTO.getIsWeb(), commandUpdateStatusDTO.getComment());
        }

        commandRepository.saveAll(commandsToUpdate);
        if (!packagesToUpdate.isEmpty()) {
            for (PackageEntity packageEntity : packagesToUpdate) {
                packageService.save(packageEntity);
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<CommandExpeditionDTO> findExpeditionCommands(Account account, LocalDateTime date) {
        LocalDateTime startDate = date.toLocalDate().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        return commandRepository.findExpeditionCommands(account, startDate, endDate);
    }

    public Optional<Command> findById(Account account, UUID id) {
        return Optional.ofNullable(commandRepository.findCommandById(account, id));
    }

    @Transactional(readOnly = true)
    public Optional<Command> findByIdHyperAdmin(UUID id) {
        return commandRepository.findById(id);
    }

    public List<HistoryCommandStatus> findCommandHistory(Account account, UUID id) {
        return commandRepository.findCommandHistory(account, id);
    }

    @Transactional
    public CommandPicture createCommandPicture(Command command, String base64Image) {
        String fileName = pictureService.saveCommandImage(command, base64Image);
        CommandPicture picture = null;

        if (fileName != null) {
            try {
                picture = new CommandPicture();
                picture.setName(fileName);
                picture.setCommand(command);
                commandPictureRepository.save(picture);

                command.getPictures().add(picture);
                commandRepository.save(command);
            } catch (Exception e) {
                pictureService.deleteImage(fileName);
            }
        }

        return picture;
    }

    @Transactional(readOnly = true)
    public List<CommandSearchResponseDTO> searchCommands(Account account, CommandSearchDTO searchDTO) {
        String name = searchDTO.getPharmacyName();
        String city = searchDTO.getPharmacyCity();
        String address = searchDTO.getPharmacyAddress();

        if (name != null) {
            for (Map.Entry<String, String> alias : GLOBAL.SEARCH_ALIASES.entrySet()) {
                if (name.toLowerCase().contains(alias.getKey())) {
                    name = name.toLowerCase().replace(alias.getKey(), alias.getValue());
                    break;
                }
            }
        }

        if (city != null) {
            for (Map.Entry<String, String> alias : GLOBAL.SEARCH_ALIASES.entrySet()) {
                if (city.toLowerCase().contains(alias.getKey())) {
                    city = city.toLowerCase().replace(alias.getKey(), alias.getValue());
                    break;
                }
            }
        }

        if (address != null) {
            for (Map.Entry<String, String> alias : GLOBAL.SEARCH_ALIASES.entrySet()) {
                if (address.toLowerCase().contains(alias.getKey())) {
                    address = address.toLowerCase().replace(alias.getKey(), alias.getValue());
                    break;
                }
            }
        }

        String commandId = searchDTO.getCommandId();
        
        // Gestion des dates : si startDate est null, ignorer la recherche par date
        LocalDateTime startDate = searchDTO.getStartDate();
        LocalDateTime endDate = searchDTO.getEndDate();
        
        if (startDate != null && endDate == null) {
            // Si seulement startDate est fournie, endDate = aujourd'hui
            endDate = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        } else if (startDate == null) {
            // Si startDate est null, ignorer complètement les filtres de date
            endDate = null;
        } else if (endDate != null) {
            // Si endDate est fournie, s'assurer qu'elle inclut toute la journée
            endDate = endDate.toLocalDate().atTime(23, 59, 59);
        }

        if (startDate == null) {
            return commandRepository.searchCommandsWithoutDates(
                    account,
                    name,
                    city,
                    searchDTO.getPharmacyCip(),
                    searchDTO.getPharmacyPostalCode(),
                    address,
                    commandId);
        } else {
            return commandRepository.searchCommandsWithDates(
                    account,
                    name,
                    city,
                    searchDTO.getPharmacyCip(),
                    searchDTO.getPharmacyPostalCode(),
                    address,
                    commandId,
                    startDate,
                    endDate);
        }
    }

    @Transactional
    public boolean updateCommandBulk(Profil profil, @Valid CommandUpdateDTO commandUpdateDTO) {
        List<Command> commandsToUpdate = new ArrayList<>();

        for (String commandId : commandUpdateDTO.getCommandIds()) {
            UUID uuid = UUID.fromString(commandId);
            Optional<Command> optCommand = findById(profil.getAccount(), uuid);
            if (optCommand.isPresent()) {
                Command command = optCommand.get();
                if (commandUpdateDTO.getExpDate() != null) {
                    command.setExpDate(commandUpdateDTO.getExpDate());
                    command.setTour(null);
                }
                if (commandUpdateDTO.getComment() != null) {
                    command.setComment(commandUpdateDTO.getComment());
                }
                if (commandUpdateDTO.getIsForced() != null) {
                    command.setIsForced(commandUpdateDTO.getIsForced());
                }
                commandsToUpdate.add(command);
            }
        }

        commandRepository.saveAll(commandsToUpdate);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Command> findLast5CommandsByPharmacyCip(Account account, String cip) {
        return commandRepository.findLast5CommandsByPharmacyCip(account, cip);
    }

    @Transactional
    public boolean updateCommandTarifBulk(Profil profil, @Valid CommandUpdateTarifDTO commandUpdateTarifDTO) {
        List<Command> commandsToUpdate = new ArrayList<>();

        for (String commandId : commandUpdateTarifDTO.getCommandIds()) {
            UUID uuid = UUID.fromString(commandId);
            Optional<Command> optCommand = findById(profil.getAccount(), uuid);
            if (optCommand.isPresent()) {
                Command command = optCommand.get();
                command.setTarif(commandUpdateTarifDTO.getTarif());
                commandsToUpdate.add(command);
            }
        }

        commandRepository.saveAll(commandsToUpdate);
        return true;
    }

    @Transactional
    public boolean deleteCommand(Account account, UUID commandId) {
        Command command = commandRepository.findCommandById(account, commandId);
        if (command == null) {
            return false;
        }

        // Supprimer tous les packages associés à cette commande
        if (command.getPackages() != null && !command.getPackages().isEmpty()) {
            for (PackageEntity packageEntity : command.getPackages()) {
                packageService.deletePackage(packageEntity.getBarcode());
            }
        }

        // Supprimer la commande elle-même
        commandRepository.delete(command);
        return true;
    }

    @Transactional
    public boolean deleteCommandHyperAdmin(UUID commandId) {
        Optional<Command> optCommand = commandRepository.findById(commandId);
        if (optCommand.isEmpty()) {
            return false;
        }

        Command command = optCommand.get();

        // 1. Supprimer tous les packages associés à cette commande (avec leur historique)
        if (command.getPackages() != null && !command.getPackages().isEmpty()) {
            for (PackageEntity packageEntity : command.getPackages()) {
                packageService.deletePackage(packageEntity.getBarcode());
            }
        }

        // 2. Supprimer l'historique de statut de la commande
        historyCommandStatusService.deleteByCommand(command);

        // 3. Supprimer la commande elle-même
        commandRepository.delete(command);
        return true;
    }
}
