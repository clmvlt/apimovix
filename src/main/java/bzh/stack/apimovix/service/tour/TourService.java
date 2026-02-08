package bzh.stack.apimovix.service.tour;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
import bzh.stack.apimovix.repository.packagerepository.HistoryPackageStatusRepository;
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
    private final HistoryPackageStatusRepository historyPackageStatusRepository;
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
            HistoryPackageStatusRepository historyPackageStatusRepository,
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
        this.historyPackageStatusRepository = historyPackageStatusRepository;
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
     * Parse une date string dans plusieurs formats possibles.
     * Formats supportes:
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd HH:mm
     * - yyyy-MM-dd'T'HH:mm:ss
     * - yyyy-MM-dd'T'HH:mm
     * - yyyy-MM-dd (converti en 08:00)
     * - ISO avec offset (ex: 2025-02-08T08:56:04+02:00)
     */
    private LocalDateTime parseFlexibleDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        String value = dateStr.trim();

        // Formats datetime a essayer
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        };

        // Essai ISO avec offset
        try {
            if (value.contains("T") && (value.contains("+") || value.endsWith("Z") || value.contains("-"))) {
                OffsetDateTime odt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                return odt.atZoneSameInstant(ZoneId.of("Europe/Paris")).toLocalDateTime();
            }
        } catch (DateTimeParseException ignored) {
        }

        // Essai format date seule
        if (value.length() == 10) {
            try {
                LocalDate date = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return date.atTime(8, 0);
            } catch (DateTimeParseException ignored) {
            }
        }

        // Essai formats datetime
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Format de date non supporte: " + dateStr +
            ". Utilisez 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm' ou 'yyyy-MM-dd HH:mm:ss'.");
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
        // Handle startDate - can be set to null via empty string
        if (tourUpdate.getStartDate() != null) {
            if (tourUpdate.shouldClearStartDate()) {
                tour.setStartDate(null);
            } else {
                tour.setStartDate(parseFlexibleDateTime(tourUpdate.getStartDate()));
            }
        }
        // Handle endDate - can be set to null via empty string
        if (tourUpdate.getEndDate() != null) {
            if (tourUpdate.shouldClearEndDate()) {
                tour.setEndDate(null);
            } else {
                tour.setEndDate(parseFlexibleDateTime(tourUpdate.getEndDate()));
            }
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

        // Recalculer les routes pour toutes les tournées affectées uniquement si autoUpdateRoute est true
        if (tourUpdateOrderDTO.isAutoUpdateRoute() && !tourIdsToUpdate.isEmpty()) {
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
        List<Tarif> tarifs = tarifService.findTarifsByAccount(account);

        // Pre-trier les tarifs par kmMax pour lookup O(1) au lieu de O(n)
        List<Tarif> sortedTarifs = tarifs.stream()
                .sorted(java.util.Comparator.comparingDouble(Tarif::getKmMax))
                .collect(Collectors.toList());

        // Query directe : charge les commandes avec pharmacy + sender + account en 1 seule requete
        // (evite de charger les tours + evite loadPharmacyInformationsForCommands)
        List<Command> allCommands = tourRepository.findCommandsForStats(account, startDate, endDate);

        // Appliquer les PharmacyInformations pour ce compte
        allCommands.forEach(command -> {
            if (command.getPharmacy() != null) {
                command.getPharmacy().loadPharmacyInformationsForAccount(account.getId());
            }
        });

        // Calcul parallele des distances (10 threads au lieu de sequentiel)
        Map<UUID, Double> distanceCache = orsService.calculateCommandDistancesBatch(allCommands);

        return allCommands.stream()
                .collect(Collectors.groupingBy(
                        command -> command.getPharmacy().getCip(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                commands -> {
                                    long orderCount = commands.size();
                                    long packageCount = commands.stream()
                                            .mapToLong(cmd -> cmd.getPackages() != null ? cmd.getPackages().size() : 0)
                                            .sum();
                                    double totalPrice = 0.0;
                                    double totalDistance = 0.0;

                                    for (Command command : commands) {
                                        Double distance = distanceCache.getOrDefault(command.getId(), 0.0);
                                        totalDistance += distance;
                                        totalPrice += calculateEstimatedTarifWithDistance(command, sortedTarifs, distance);
                                    }

                                    double averageDistance = orderCount > 0 ? totalDistance / orderCount : 0.0;

                                    List<PharmacyOrderStatsDTO.CommandStatsDTO> commandStats = commands.stream()
                                            .sorted((c1, c2) -> {
                                                if (c1.getTourOrder() == null && c2.getTourOrder() == null) return 0;
                                                if (c1.getTourOrder() == null) return 1;
                                                if (c2.getTourOrder() == null) return -1;
                                                return Integer.compare(c1.getTourOrder(), c2.getTourOrder());
                                            })
                                            .map(command -> new PharmacyOrderStatsDTO.CommandStatsDTO(
                                                    command, sortedTarifs, distanceCache.getOrDefault(command.getId(), 0.0)))
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

    private Double calculateEstimatedTarifWithDistance(Command command, List<Tarif> sortedTarifs, Double distance) {
        if (command.getTarif() != null) {
            return command.getTarif();
        }

        // sortedTarifs est pre-trie par kmMax : findFirst remplace filter+min
        Optional<Tarif> matchingTarif = sortedTarifs.stream()
                .filter(tarif -> distance <= tarif.getKmMax())
                .findFirst();

        return matchingTarif.map(Tarif::getPrixEuro).orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getLoadingTimesByTourIds(List<String> tourIds) {
        Map<String, Double> loadingTimes = new HashMap<>();
        if (tourIds == null || tourIds.isEmpty()) {
            return loadingTimes;
        }

        List<Object[]> results = historyPackageStatusRepository.findLoadingTimesByTourIds(tourIds);
        for (Object[] row : results) {
            String tourId = (String) row[0];
            Timestamp firstLoaded = (Timestamp) row[1];
            Timestamp livraisonAt = (Timestamp) row[2];

            if (firstLoaded != null && livraisonAt != null) {
                long millis = Duration.between(
                        firstLoaded.toLocalDateTime(),
                        livraisonAt.toLocalDateTime()
                ).toMillis();
                double minutes = BigDecimal.valueOf(millis)
                        .divide(BigDecimal.valueOf(60000), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                loadingTimes.put(tourId, minutes);
            }
        }

        return loadingTimes;
    }
}
