package bzh.stack.apimovix.service.tour;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.command.CommandUpdateOrderDTO;
import bzh.stack.apimovix.dto.command.CommandValidateLoadingDTO;
import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.dto.tour.PharmacyOrderStatsDTO;
import bzh.stack.apimovix.dto.tour.TourCreateDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateOrderDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateStatusDTO;
import bzh.stack.apimovix.dto.tour.ValidateLoadingRequestDTO;
import bzh.stack.apimovix.dto.tour.ValidateLoadingResponseDTO;
import bzh.stack.apimovix.enums.ResponseStatusENUM;
import bzh.stack.apimovix.mapper.TourMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.Zone;
import bzh.stack.apimovix.model.History.HistoryTourStatus;
import bzh.stack.apimovix.model.StatusType.CommandStatus;
import bzh.stack.apimovix.model.StatusType.TourStatus;
import bzh.stack.apimovix.repository.ZoneRepository;
import bzh.stack.apimovix.repository.tour.HistoryTourStatusRepository;
import bzh.stack.apimovix.repository.tour.TourRepository;
import bzh.stack.apimovix.service.ORSService;
import bzh.stack.apimovix.service.ProfileService;
import bzh.stack.apimovix.service.TarifService;
import bzh.stack.apimovix.service.command.CommandService;
import bzh.stack.apimovix.service.command.CommandStatusService;
import jakarta.validation.Valid;

@Service
public class TourService {
    private final TourMapper tourMapper;
    private final TourRepository tourRepository;
    private final HistoryTourStatusService historyTourStatusService;
    private final TourStatusService tourStatusService;
    private final ProfileService profileService;
    private final CommandService commandService;
    private final CommandStatusService commandStatusService;
    private final HistoryTourStatusRepository historyTourStatusRepository;
    private final ORSService orsService;
    private final TarifService tarifService;
    private final ZoneRepository zoneRepository;
    private final TourCommandService tourCommandService;

    public TourService(
            TourMapper tourMapper,
            TourRepository tourRepository,
            HistoryTourStatusService historyTourStatusService,
            TourStatusService tourStatusService,
            ProfileService profileService,
            CommandService commandService,
            CommandStatusService commandStatusService,
            HistoryTourStatusRepository historyTourStatusRepository,
            ORSService orsService,
            TarifService tarifService,
            ZoneRepository zoneRepository,
            @Lazy TourCommandService tourCommandService) {
        this.tourMapper = tourMapper;
        this.tourRepository = tourRepository;
        this.historyTourStatusService = historyTourStatusService;
        this.tourStatusService = tourStatusService;
        this.profileService = profileService;
        this.commandService = commandService;
        this.commandStatusService = commandStatusService;
        this.historyTourStatusRepository = historyTourStatusRepository;
        this.orsService = orsService;
        this.tarifService = tarifService;
        this.zoneRepository = zoneRepository;
        this.tourCommandService = tourCommandService;
    }

    /**
     * Charge les PharmacyInformationsList et applique les PharmacyInformations pour un compte donné
     */
    private void loadPharmacyInformationsForCommands(List<Command> commands, UUID accountId) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        // Récupérer tous les CIPs des pharmacies
        List<String> pharmacyCips = commands.stream()
            .filter(c -> c.getPharmacy() != null)
            .map(c -> c.getPharmacy().getCip())
            .distinct()
            .collect(Collectors.toList());

        if (!pharmacyCips.isEmpty()) {
            // Charger les PharmacyInformationsList pour toutes les pharmacies en une seule requête
            tourRepository.loadPharmacyInformationsByCips(pharmacyCips);

            // Appliquer les PharmacyInformations pour ce compte
            commands.forEach(command -> {
                if (command.getPharmacy() != null) {
                    command.getPharmacy().loadPharmacyInformationsForAccount(accountId);
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public Optional<Tour> findTour(Account account, String tourId) {
        Tour tour = tourRepository.findTour(account, tourId);
        if (tour != null && tour.getCommands() != null) {
            loadPharmacyInformationsForCommands(tour.getCommands(), account.getId());
        }
        return Optional.ofNullable(tour);
    }

    @Transactional(readOnly = true)
    public Optional<Tour> findTourForTarif(Account account, String tourId) {
        Tour tour = tourRepository.findTourForTarif(account, tourId);
        if (tour != null && tour.getCommands() != null) {
            loadPharmacyInformationsForCommands(tour.getCommands(), account.getId());
        }
        return Optional.ofNullable(tour);
    }

    @Transactional(readOnly = true)
    public List<Tour> findTours(Account account, LocalDate date) {
        // Charger les tours sans les commands pour éviter le produit cartésien
        List<Tour> tours = tourRepository.findToursOptimizedByDate(account, date);

        if (!tours.isEmpty()) {
            // Charger les commands séparément pour tous les tours
            List<String> tourIds = tours.stream().map(Tour::getId).collect(java.util.stream.Collectors.toList());
            List<bzh.stack.apimovix.model.Command> commands = tourRepository.findCommandsByTourIds(account, tourIds);

            // Charger les PharmacyInformations pour chaque pharmacy dans les commands
            commands.forEach(command -> {
                if (command.getPharmacy() != null) {
                    command.getPharmacy().loadPharmacyInformationsForAccount(account.getId());
                }
            });

            // Grouper les commands par tour ID
            java.util.Map<String, List<bzh.stack.apimovix.model.Command>> commandsByTourId =
                commands.stream().collect(java.util.stream.Collectors.groupingBy(
                    command -> command.getTour().getId()
                ));

            // Assigner les commands aux tours
            tours.forEach(tour -> {
                List<bzh.stack.apimovix.model.Command> tourCommands = commandsByTourId.getOrDefault(tour.getId(), new java.util.ArrayList<>());
                tour.setCommands(tourCommands);
            });
        }

        return tours;
    }

    @Transactional(readOnly = true)
    public List<Tour> findToursByDateRange(Account account, LocalDate startDate, LocalDate endDate) {
        List<Tour> tours = tourRepository.findByDateRange(account, startDate, endDate);
        // Charger les PharmacyInformations pour toutes les commands de tous les tours
        List<Command> allCommands = tours.stream()
            .flatMap(tour -> tour.getCommands() != null ? tour.getCommands().stream() : java.util.stream.Stream.empty())
            .collect(Collectors.toList());
        loadPharmacyInformationsForCommands(allCommands, account.getId());
        return tours;
    }

    @Transactional
    public Optional<Tour> assignTour(Account account, String tourId, UUID profilId) {
        Optional<Profil> optProfil = profileService.findProfile(account, profilId);
        if (optProfil.isEmpty()) {
            return Optional.empty();
        }
        return assignTour(account, tourId, optProfil.get());
    }

    @Transactional
    public Optional<Tour> assignTour(Account account, String tourId, Profil profil) {
        Optional<Tour> optTour = findTour(account, tourId);

        if (optTour.isEmpty()) {
            return Optional.empty();
        }

        Tour tour = optTour.get();
        tour.setProfil(profil);

        return Optional.of(tourRepository.save(tour));
    }

    @Transactional
    public Optional<Tour> updateTour(Account account, String tourId, TourUpdateDTO tourUpdate, Profil profil) {
        Optional<Tour> optTour = findTour(account, tourId);

        if (optTour.isEmpty()) {
            return Optional.empty();
        }

        Tour tour = optTour.get();

        if (tourUpdate.getName() != null) {
            tour.setName(tourUpdate.getName());
        }
        // Handle immat - can be set to null via empty string or "null" value
        if (tourUpdate.getImmat() != null) {
            if (tourUpdate.shouldClearImmat()) {
                tour.setImmat(null);
            } else {
                tour.setImmat(tourUpdate.getImmat());
            }
        }
        // Handle startKm - use -1 as sentinel value to set to null
        if (tourUpdate.getStartKm() != null) {
            if (tourUpdate.shouldClearStartKm()) {
                tour.setStartKm(null);
            } else {
                tour.setStartKm(tourUpdate.getStartKm());
            }
        }
        // Handle endKm - use -1 as sentinel value to set to null
        if (tourUpdate.getEndKm() != null) {
            if (tourUpdate.shouldClearEndKm()) {
                tour.setEndKm(null);
            } else {
                tour.setEndKm(tourUpdate.getEndKm());
            }
        }
        if (tourUpdate.getInitialDate() != null) {
            tour.setInitialDate(tourUpdate.getInitialDate());
        }
        if (tourUpdate.getStartDate() != null) {
            tour.setStartDate(tourUpdate.getStartDate());
        }
        if (tourUpdate.getEndDate() != null) {
            tour.setEndDate(tourUpdate.getEndDate());
        }
        if (tourUpdate.getColor() != null) {
            tour.setColor(tourUpdate.getColor());
        }
        if (tourUpdate.getEstimateMins() != null) {
            tour.setEstimateMins(tourUpdate.getEstimateMins());
        }
        if (tourUpdate.getEstimateKm() != null) {
            tour.setEstimateKm(tourUpdate.getEstimateKm());
        }
        if (tourUpdate.getGeometry() != null) {
            tour.setGeometry(tourUpdate.getGeometry());
        }
        if (tourUpdate.getZoneId() != null) {
            Optional<Zone> optZone = zoneRepository.findZone(account, tourUpdate.getZoneId());
            tour.setZone(optZone.orElse(null));
        }

        return Optional.of(tourRepository.save(tour));
    }

    @Transactional
    public Tour updateTourStatus(Profil profil, TourStatus status, Tour tour) {
        HistoryTourStatus historyTourStatus = historyTourStatusService.createHistoryTourStatus(tour, profil, status);
        tour.setLastHistoryStatus(historyTourStatus);
        return tourRepository.save(tour);
    }

    @Transactional
    public boolean updateTourStatusBulk(Profil profil, TourUpdateStatusDTO tourStatusDTO) {
        Optional<TourStatus> optStatus = tourStatusService.findTourStatusById(tourStatusDTO.getStatusId());
        if (optStatus.isEmpty()) {
            return false;
        }
        TourStatus status = optStatus.get();

        List<Tour> tours = tourRepository.findAllByIdIn(profil.getAccount(), tourStatusDTO.getTourIds());
        if (tours.size() != tourStatusDTO.getTourIds().size()) {
            return false;
        }

        List<HistoryTourStatus> historyStatuses = tours.stream()
            .map(tour -> historyTourStatusService.createHistoryTourStatus(tour, profil, status))
            .collect(Collectors.toList());

        tours.forEach(tour -> tour.setLastHistoryStatus(
            historyStatuses.stream()
                .filter(hs -> hs.getTour().getId().equals(tour.getId()))
                .findFirst()
                .orElse(null)
        ));

        tourRepository.saveAll(tours);

        return true;
    }

    @Transactional(readOnly = true)
    public List<Tour> findToursByProfile(Account account, Profil profil) {
        List<Tour> tours = tourRepository.findByProfile(account, profil);
        // Charger les PharmacyInformations pour toutes les commands de tous les tours
        List<Command> allCommands = tours.stream()
            .flatMap(tour -> tour.getCommands() != null ? tour.getCommands().stream() : java.util.stream.Stream.empty())
            .collect(Collectors.toList());
        loadPharmacyInformationsForCommands(allCommands, account.getId());
        return tours;
    }

    @Transactional(readOnly = true)
    public List<HistoryTourStatus> findTourHistory(Account account, String tourId) {
        Optional<Tour> optTour = findTour(account, tourId);
        if (optTour.isEmpty()) {
            return new ArrayList<>();
        }
        return historyTourStatusRepository.findByTourIdOrderByCreatedAtDesc(tourId);
    }

    @Transactional
    public Optional<ValidateLoadingResponseDTO> validateLoading(Profil profil,
            ValidateLoadingRequestDTO validateLoadingRequestDTO, String tourId) {
        ValidateLoadingResponseDTO response = new ValidateLoadingResponseDTO();

        Optional<Tour> optTour = findTour(profil.getAccount(), tourId);
        if (optTour.isEmpty()) {
            return Optional.empty();
        }
        Tour tour = optTour.get();

        boolean isCompareValide = compareValidateLoading(tour.getCommands(), validateLoadingRequestDTO.getCommands());
        if (!isCompareValide) {
            response.setMessage("Des changements ont été apportés aux commandes, merci de rafraîchir la tournée.");
            response.setResponseCode(207);
            response.setStatus(ResponseStatusENUM.REFRESH_ERROR);
        } else {
            Optional<TourStatus> tourStatus = tourStatusService.findTourStatusById(3);
            if (tourStatus.isEmpty()) {
                return Optional.empty();
            }
            updateTourStatus(profil, tourStatus.get(), tour);
            tour.setStartDate(LocalDateTime.now(ZoneId.of("Europe/Paris")));
            tour = tourRepository.save(tour);
            for (CommandValidateLoadingDTO commandDTO : validateLoadingRequestDTO.getCommands()) {
                Optional<Command> command = commandService.findById(profil.getAccount(), commandDTO.getCommandId());
                if (command.isPresent()) {
                    Optional<CommandStatus> commandStatus = commandStatusService
                            .findCommandStatus(commandDTO.getStatus().getId());
                    if (commandStatus.isPresent()) {
                        commandService.updateCommandStatus(profil, commandStatus.get(), command.get(), false, commandDTO.getComment());
                    }
                }
            }
            response.setMessage("Chargement validé avec succès.");
            response.setResponseCode(201);
            response.setStatus(ResponseStatusENUM.SUCCESS);
        }

        return Optional.of(response);
    }

    private boolean compareValidateLoading(List<Command> tourCommands, List<CommandValidateLoadingDTO> commands) {
        if (tourCommands.size() != commands.size()) {
            return false;
        }

        Map<String, CommandValidateLoadingDTO> commandsMap = commands.stream()
                .collect(Collectors.toMap(
                        command -> command.getCommandId() != null ? command.getCommandId().toString()
                                : command.getPackages().stream()
                                        .map(PackageDTO::getBarcode)
                                        .sorted()
                                        .collect(Collectors.joining("_")),
                        command -> command,
                        (existing, replacement) -> existing // En cas de doublon, garder la première occurrence
                ));

        for (Command tourCommand : tourCommands) {
            String key = tourCommand.getId() != null ? tourCommand.getId().toString()
                    : tourCommand.getPackages().stream()
                            .map(PackageEntity::getBarcode)
                            .sorted()
                            .collect(Collectors.joining("_"));

            CommandValidateLoadingDTO matchingCommand = commandsMap.get(key);
            if (matchingCommand == null) {
                return false;
            }

            Map<String, PackageDTO> packagesMap = matchingCommand.getPackages().stream()
                    .collect(Collectors.toMap(PackageDTO::getBarcode, packageDTO -> packageDTO));

            if (tourCommand.getPackages().size() != matchingCommand.getPackages().size()) {
                return false;
            }

            for (PackageEntity tourPackage : tourCommand.getPackages()) {
                if (!packagesMap.containsKey(tourPackage.getBarcode())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Transactional
    public boolean updateTourOrder(Account account, TourUpdateOrderDTO tourUpdateOrderDTO) {
        // Collecter toutes les tournées affectées
        java.util.Set<String> tourIdsToUpdate = new java.util.HashSet<>();

        for (CommandUpdateOrderDTO commandUpdateOrderDTO : tourUpdateOrderDTO.getCommands()) {
            Optional<Command> optCommand = commandService.findById(account, commandUpdateOrderDTO.getCommandId());
            if (optCommand.isPresent()) {
                Command command = optCommand.get();
                command.setTourOrder(commandUpdateOrderDTO.getTourOrder());
                commandService.save(command);

                // Ajouter l'ID de la tournée à la liste des tournées à mettre à jour
                if (command.getTour() != null) {
                    tourIdsToUpdate.add(command.getTour().getId());
                }
            } else {
                return false;
            }
        }

        // Recalculer les routes pour toutes les tournées affectées en utilisant TourCommandService
        if (!tourIdsToUpdate.isEmpty()) {
            tourCommandService.updateTourRoutes(account, tourIdsToUpdate);
        }

        return true;
    }

    @Transactional
    public Tour createTour(Profil profil, @Valid TourCreateDTO tourCreateDTO) {
        return createTour(profil, tourCreateDTO, false);
    }

    @Transactional
    public Tour createTour(Profil profil, @Valid TourCreateDTO tourCreateDTO, boolean assignProfil) {
        Tour tour = tourMapper.toCreateEntity(tourCreateDTO);
        tour.setId(generateNewId());
        tour.setAccount(profil.getAccount());

        // Assigner le profil uniquement si demandé (pour le scheduler)
        if (assignProfil) {
            tour.setProfil(profil);
        }

        // Gérer la zone si fournie
        if (tourCreateDTO.getZoneId() != null) {
            Optional<Zone> optZone = zoneRepository.findZone(profil.getAccount(), tourCreateDTO.getZoneId());
            tour.setZone(optZone.orElse(null));
        }

        tourRepository.save(tour);

        Optional<TourStatus> optStatus = tourStatusService.findTourStatusById(2);
        if (optStatus.isPresent()) {
            HistoryTourStatus hs = historyTourStatusService.createHistoryTourStatus(tour, profil, optStatus.get());
            tour.setLastHistoryStatus(hs);
        }

        return tourRepository.save(tour);
    }

    public String generateTourId() {
        StringBuilder tourId = new StringBuilder();
        String hexChars = "0123456789abcdef";
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(hexChars.length());
            tourId.append(hexChars.charAt(index));
        }

        return tourId.toString();
    }

    @Transactional(readOnly = true)
    public String generateNewId() {
        String tourId;
        do {
            tourId = generateTourId();
        } while (tourRepository.findById(tourId).orElse(null) != null);

        return tourId;
    }

    @Transactional
    public boolean deleteTour(Account account, String tourId) {
        Optional<Tour> optTour = findTour(account, tourId);
        if (optTour.isEmpty()) {
            return false;
        }

        Tour tour = optTour.get();

        if (tour.getCommands() != null) {
            for (Command command : tour.getCommands()) {
                command.setTour(null);
                command.setTourOrder(0);
                commandService.save(command);
            }
        }

        if (tour.getLastHistoryStatus() != null) {
            historyTourStatusRepository.deleteByTourId(tour.getId());
        }

        tourRepository.delete(tour);
        return true;
    }

    @Transactional
    public Optional<Tour> unassignTour(Account account, String tourId) {
        Optional<Tour> optTour = findTour(account, tourId);

        if (optTour.isEmpty()) {
            return Optional.empty();
        }

        Tour tour = optTour.get();
        tour.setProfil(null);

        return Optional.of(tourRepository.save(tour));
    }

    @Transactional(readOnly = true)
    public List<PharmacyOrderStatsDTO> getPharmacyOrderStats(Account account, LocalDate startDate, LocalDate endDate) {
        List<Tour> tours = findToursByDateRange(account, startDate, endDate);
        List<Tarif> tarifs = tarifService.findTarifsByAccount(account);

        // Charger les PharmacyInformations pour chaque pharmacy dans les commands (déjà fait dans findToursByDateRange)

        return tours.stream()
                .flatMap(tour -> tour.getCommands().stream())
                .filter(command -> command.getPharmacy() != null)
                .collect(Collectors.groupingBy(
                        command -> command.getPharmacy(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                commands -> {
                                    // Calculer les statistiques pour cette pharmacie
                                    long orderCount = commands.size();
                                    long packageCount = commands.stream().mapToLong(cmd -> cmd.getPackages() != null ? cmd.getPackages().size() : 0).sum();
                                    double totalPrice = 0.0;
                                    double totalDistance = 0.0;
                                    
                                    for (Command command : commands) {
                                        Double distance = orsService.calculateCommandDistance(command).orElse(0.0);
                                        totalDistance += distance;
                                        
                                        // Calculer le tarif pour cette commande
                                        totalPrice += calculateEstimatedTarif(command, tarifs);
                                    }
                                    
                                    double averageDistance = orderCount > 0 ? totalDistance / orderCount : 0.0;
                                    
                                    // Convertir les commandes en CommandStatsDTO
                                    List<PharmacyOrderStatsDTO.CommandStatsDTO> commandStats = commands.stream()
                                            .sorted((c1, c2) -> {
                                                if (c1.getTourOrder() == null && c2.getTourOrder() == null) return 0;
                                                if (c1.getTourOrder() == null) return 1;
                                                if (c2.getTourOrder() == null) return -1;
                                                return Integer.compare(c1.getTourOrder(), c2.getTourOrder());
                                            })
                                            .map(command -> new PharmacyOrderStatsDTO.CommandStatsDTO(command, tarifs, orsService))
                                            .collect(Collectors.toList());
                                    
                                    return new PharmacyOrderStatsDTO(
                                            new bzh.stack.apimovix.dto.pharmacy.PharmacyDTO(commands.get(0).getPharmacy()),
                                            orderCount,
                                            packageCount,
                                            totalPrice,
                                            averageDistance,
                                            commandStats
                                    );
                                }
                        )
                ))
                .values()
                .stream()
                .sorted((a, b) -> {
                    String cipA = a.getPharmacy().getCip();
                    String cipB = b.getPharmacy().getCip();
                    if (cipA == null && cipB == null) return 0;
                    if (cipA == null) return 1;
                    if (cipB == null) return -1;
                    return cipA.compareTo(cipB);
                })
                .collect(Collectors.toList());
    }

    private Double calculateEstimatedTarif(Command command, List<Tarif> tarifs) {
        if (command.getTarif() != null) {
            return command.getTarif();
        }

        // Calculer le tarif estimé basé sur la distance
        Double distance = orsService.calculateCommandDistance(command).orElse(0.0);
        Optional<Tarif> matchingTarif = tarifs.stream()
                .filter(tarif -> distance <= tarif.getKmMax())
                .min((t1, t2) -> Double.compare(t1.getKmMax(), t2.getKmMax()));

        return matchingTarif.map(Tarif::getPrixEuro).orElse(0.0);
    }
}
