package bzh.stack.apimovix.service.tour;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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


        Optional<Tour> optTour = tourService.findTour(account, tourId);
        if (optTour.isEmpty()) {
            return false;
        }
        Tour tour = optTour.get();

        Integer lastTourOrder = 0;
        lastTourOrder = tour.getCommands().isEmpty() ? 0
                : tour.getCommands().stream()
                        .mapToInt(Command::getTourOrder)
                        .max()
                        .orElse(0);

        AtomicInteger tourOrderCounter = new AtomicInteger(lastTourOrder);

        List<Tour> toursToUpdate = new ArrayList<>();
        toursToUpdate.add(tour);

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

        commandsToUpdate.forEach(command -> {
            if (command.getTour() != null && !toursToUpdate.contains(command.getTour())) {
                toursToUpdate.add(command.getTour());
            }
        });

        commandsToUpdate.forEach(command -> {
            command.setTourOrder(tourOrderCounter.incrementAndGet());
            command.setTour(tour);
        });

        commandRepository.saveAll(commandsToUpdate);
        tourRepository.saveAll(toursToUpdate);
        
        entityManager.flush();
        entityManager.clear();

        toursToUpdate.forEach(t -> {
            Tour reloadedTour = tourRepository.findTour(account, t.getId());
            if (reloadedTour != null) {
                Optional<RouteResponseDTO> tourRouteOptional = orsService.calculateTourRoute(reloadedTour);
                if (tourRouteOptional.isEmpty()) {
                    t.setGeometry(null);
                    t.setEstimateKm(0.0);
                    t.setEstimateMins(0.0);
                } else {
                    RouteResponseDTO tourRoute = tourRouteOptional.get();
                    t.setGeometry(tourRoute.getGeometry());
                    t.setEstimateKm(tourRoute.getDistance());
                    t.setEstimateMins(tourRoute.getDuration());
                }
            }
        });

        tourRepository.saveAll(toursToUpdate);

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

        List<Tour> toursToUpdate = new ArrayList<>();

        commandsToUpdate.forEach(command -> {
            if (command.getTour() != null) {
                Tour tourToUpdate = command.getTour();
                if (!toursToUpdate.contains(tourToUpdate)) {
                    toursToUpdate.add(tourToUpdate);
                }
                command.setTourOrder(null);
                command.setTour(null);
            }
        });


        commandRepository.saveAll(commandsToUpdate);
        
        entityManager.flush();
        entityManager.clear();

        toursToUpdate.forEach(t -> {
            Tour reloadedTour = tourRepository.findTour(account, t.getId());
            if (reloadedTour != null) {
                Optional<RouteResponseDTO> tourRouteOptional = orsService.calculateTourRoute(reloadedTour);
                if (tourRouteOptional.isEmpty()) {
                    t.setGeometry(null);
                    t.setEstimateKm(0.0);
                    t.setEstimateMins(0.0);
                } else {
                    RouteResponseDTO tourRoute = tourRouteOptional.get();
                    t.setGeometry(tourRoute.getGeometry());
                    t.setEstimateKm(tourRoute.getDistance());
                    t.setEstimateMins(tourRoute.getDuration());
                }
            }
        });

        tourRepository.saveAll(toursToUpdate);

        return true;
    }
}