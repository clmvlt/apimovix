package bzh.stack.apimovix.service.tour;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.command.CommandIdsDTO;
import bzh.stack.apimovix.dto.ors.RouteResponseDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.repository.command.CommandRepository;
import bzh.stack.apimovix.repository.tour.TourRepository;
import bzh.stack.apimovix.service.ORSService;
import bzh.stack.apimovix.service.command.CommandService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TourCommandService {
    private final CommandRepository commandRepository;
    private final TourRepository tourRepository;
    private final TourService tourService;
    private final CommandService commandService;
    private final ORSService orsService;
    private final EntityManager entityManager;

    @Transactional
    public boolean assignCommandsToTour(Account account, CommandIdsDTO commandIds, String tourId) {
        if (commandIds.getCommandIds().isEmpty()) {
            return false;
        }

        List<UUID> commandUuids = commandIds.getCommandIds().stream()
                .map(id -> {
                    try {
                        return UUID.fromString(id);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(uuid -> uuid != null)
                .collect(Collectors.toList());

        if (commandUuids.isEmpty()) {
            return false;
        }

        Optional<Tour> optTour = tourService.findTour(account, tourId);
        if (optTour.isEmpty()) {
            return false;
        }
        Tour tour = optTour.get();

        List<Command> commandsToUpdate = commandService.findCommandsByIds(account, commandUuids);
        if (commandsToUpdate.isEmpty()) {
            return false;
        }

        Set<String> previousTourIds = commandsToUpdate.stream()
                .map(cmd -> cmd.getTour() != null ? cmd.getTour().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        commandsToUpdate.forEach(command -> {
            command.setTour(tour);
            command.setTourOrder(9999);
        });

        commandRepository.saveAll(commandsToUpdate);

        entityManager.flush();
        entityManager.clear();

        Set<String> toursToReorganize = new java.util.HashSet<>(previousTourIds);
        toursToReorganize.add(tourId);

        for (String affectedTourId : toursToReorganize) {
            reorganizeTourOrder(account, affectedTourId);
        }

        entityManager.flush();
        entityManager.clear();

        updateTourRoutes(account, toursToReorganize);

        return true;
    }

    private void reorganizeTourOrder(Account account, String tourId) {
        Tour reloadedTour = tourRepository.findTour(account, tourId);
        if (reloadedTour != null && reloadedTour.getCommands() != null) {
            List<Command> commands = reloadedTour.getCommands().stream()
                .filter(cmd -> cmd.getTour() != null && cmd.getTour().getId().equals(tourId))
                .sorted((c1, c2) -> {
                    if (c1.getTourOrder() == null) return 1;
                    if (c2.getTourOrder() == null) return -1;
                    return c1.getTourOrder().compareTo(c2.getTourOrder());
                })
                .collect(Collectors.toList());

            AtomicInteger newOrder = new AtomicInteger(0);
            commands.forEach(cmd -> cmd.setTourOrder(newOrder.incrementAndGet()));

            if (!commands.isEmpty()) {
                commandRepository.saveAll(commands);
            }
        }
    }

    public void updateTourRoutes(Account account, Set<String> tourIds) {
        List<Tour> toursToUpdate = tourIds.stream()
                .map(id -> tourRepository.findTour(account, id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Charger les PharmacyInformationsList et appliquer les PharmacyInformations
        List<Command> allCommands = toursToUpdate.stream()
            .flatMap(tour -> tour.getCommands() != null ? tour.getCommands().stream() : java.util.stream.Stream.empty())
            .collect(Collectors.toList());

        if (!allCommands.isEmpty()) {
            List<String> pharmacyCips = allCommands.stream()
                .filter(c -> c.getPharmacy() != null)
                .map(c -> c.getPharmacy().getCip())
                .distinct()
                .collect(Collectors.toList());

            if (!pharmacyCips.isEmpty()) {
                tourRepository.loadPharmacyInformationsByCips(pharmacyCips);
                allCommands.forEach(command -> {
                    if (command.getPharmacy() != null) {
                        command.getPharmacy().loadPharmacyInformationsForAccount(account.getId());
                    }
                });
            }
        }

        toursToUpdate.parallelStream().forEach(tour -> {
            Optional<RouteResponseDTO> tourRouteOptional = orsService.calculateTourRoute(tour);
            if (tourRouteOptional.isEmpty()) {
                tour.setGeometry(null);
                tour.setEstimateKm(0.0);
                tour.setEstimateMins(0.0);
            } else {
                RouteResponseDTO tourRoute = tourRouteOptional.get();
                tour.setGeometry(tourRoute.getGeometry());
                tour.setEstimateKm(tourRoute.getDistance());
                tour.setEstimateMins(tourRoute.getDuration());
            }
        });

        tourRepository.saveAll(toursToUpdate);
    }

    @Transactional
    public boolean addTourToCommand(Account account, Command command, Tour tour) {
        if (command == null || tour == null) {
            return false;
        }

        String previousTourId = command.getTour() != null ? command.getTour().getId() : null;

        command.setTour(tour);
        command.setTourOrder(9999);
        commandRepository.save(command);

        entityManager.flush();
        entityManager.clear();

        Set<String> toursToReorganize = new java.util.HashSet<>();
        if (previousTourId != null) {
            toursToReorganize.add(previousTourId);
        }
        toursToReorganize.add(tour.getId());

        for (String tourId : toursToReorganize) {
            reorganizeTourOrder(account, tourId);
        }

        entityManager.flush();
        entityManager.clear();

        updateTourRoutes(account, toursToReorganize);

        return true;
    }

    @Transactional
    public boolean unassignCommandsFromTour(Account account, CommandIdsDTO commandIds) {
        if (commandIds.getCommandIds().isEmpty()) {
            return false;
        }

        List<UUID> commandUuids = commandIds.getCommandIds().stream()
                .map(id -> {
                    try {
                        return UUID.fromString(id);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(uuid -> uuid != null)
                .collect(Collectors.toList());

        if (commandUuids.isEmpty()) {
            return false;
        }

        List<Command> commandsToUpdate = commandService.findCommandsByIds(account, commandUuids);

        if (commandsToUpdate.isEmpty()) {
            return false;
        }

        Set<String> affectedTourIds = commandsToUpdate.stream()
                .filter(cmd -> cmd.getTour() != null)
                .map(cmd -> cmd.getTour().getId())
                .collect(Collectors.toSet());

        if (affectedTourIds.isEmpty()) {
            return false;
        }

        commandsToUpdate.forEach(command -> {
            command.setTourOrder(null);
            command.setTour(null);
        });

        commandRepository.saveAll(commandsToUpdate);

        entityManager.flush();
        entityManager.clear();

        affectedTourIds.forEach(tourId -> {
            reorganizeTourOrder(account, tourId);
        });

        entityManager.flush();
        entityManager.clear();

        updateTourRoutes(account, affectedTourIds);

        return true;
    }
}