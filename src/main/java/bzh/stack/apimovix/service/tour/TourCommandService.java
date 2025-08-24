package bzh.stack.apimovix.service.tour;

import java.util.ArrayList;
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

        // Convertir les IDs et valider
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

        // Utiliser une requête bulk pour récupérer tour et commands en une fois
        Optional<Tour> optTour = tourService.findTour(account, tourId);
        if (optTour.isEmpty()) {
            return false;
        }
        Tour tour = optTour.get();

        // Récupérer les commands à assigner
        List<Command> commandsToUpdate = commandService.findCommandsByIds(account, commandUuids);
        if (commandsToUpdate.isEmpty()) {
            return false;
        }

        // Calculer le dernier tour order une seule fois
        Integer maxTourOrder = commandRepository.findMaxTourOrderByTourId(tourId);
        AtomicInteger tourOrderCounter = new AtomicInteger(maxTourOrder != null ? maxTourOrder : 0);

        // Collecter les tours affectés (pour mise à jour des routes)
        Set<String> affectedTourIds = commandsToUpdate.stream()
                .map(cmd -> cmd.getTour() != null ? cmd.getTour().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        affectedTourIds.add(tourId);

        // Mise à jour des commands en une seule opération
        commandsToUpdate.forEach(command -> {
            command.setTourOrder(tourOrderCounter.incrementAndGet());
            command.setTour(tour);
        });

        // Une seule sauvegarde pour les commands
        commandRepository.saveAll(commandsToUpdate);

        // Mise à jour asynchrone des routes si possible, sinon en parallèle
        updateTourRoutes(account, affectedTourIds);

        return true;
    }

    private void updateTourRoutes(Account account, Set<String> tourIds) {
        List<Tour> toursToUpdate = tourIds.stream()
                .map(id -> tourRepository.findTour(account, id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Traitement parallèle des routes pour améliorer les performances
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

        // Une seule sauvegarde pour tous les tours
        tourRepository.saveAll(toursToUpdate);
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