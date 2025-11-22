package bzh.stack.apimovix.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.importer.SendCommandRequestDTO;
import bzh.stack.apimovix.dto.importer.SendCommandResponseDTO;
import bzh.stack.apimovix.dto.ors.RouteResponseDTO;
import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyCreateDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.PharmacyInformations;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.repository.tour.TourRepository;
import bzh.stack.apimovix.service.command.CommandService;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
import bzh.stack.apimovix.service.tour.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImporterService {
    private final PharmacyService pharmacyService;
    private final CommandService commandService;
    private final PackageService packageService;
    private final SenderService senderService;
    private final TourService tourService;
    private final TourRepository tourRepository;
    private final ORSService orsService;

    @Transactional
    public SendCommandResponseDTO sendCommand(@Valid SendCommandRequestDTO body) {
        try {
            // D'abord récupérer ou créer le sender (de façon synchrone)
            Optional<Sender> optSender = senderService.findSender(body.getSender().getCode());
            Sender sender = optSender.orElseGet(() -> senderService.createSender(body.getSender()));

            // Ensuite, créer ou récupérer la pharmacy (uniquement par CIP, sans vérifier l'account)
            Optional<Pharmacy> optPharmacy = pharmacyService.findPharmacyByCipOnly(body.getRecipient().getCip());
            Pharmacy pharmacy;
            if (optPharmacy.isPresent()) {
                pharmacy = optPharmacy.get();
            } else {
                // Si la pharmacy n'existe pas, on l'associe au compte du sender
                PharmacyCreateDTO pharmacyDTO = body.getRecipient();
                pharmacy = pharmacyService.createPharmacy(sender.getAccount(), pharmacyDTO);
            }

            // Vérifier si c'est une nouvelle commande avant de la créer
            boolean isNewCommand = commandService.findPharmacyCommandByDate(sender.getAccount(), pharmacy.getCip(), body.getExpedition_date()).isEmpty();

            Command command = createOrUpdateCommand(pharmacy, sender, body);

            List<PackageEntity> packages = createPackagesInParallel(command, body);

            command.setPackages(packages);
            command = commandService.save(command);

            // Associer automatiquement la commande à une tournée en fonction de la zone
            // uniquement lors de la création de la commande
            if (isNewCommand) {
                assignCommandToTourByZone(command);
            }

            return buildResponse(command, body.getCommand().getPackages());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du traitement de la commande", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Command createOrUpdateCommand(Pharmacy pharmacy, Sender sender, SendCommandRequestDTO body) {
        Optional<Command> optCommand = commandService.findPharmacyCommandByDate(sender.getAccount(), pharmacy.getCip(), body.getExpedition_date());

        if (optCommand.isEmpty()) {
            // Charger le PharmacyInformations pour ce compte
            pharmacy.loadPharmacyInformationsForAccount(sender.getAccount().getId());

            // Obtenir ou créer le PharmacyInformations pour ce compte
            PharmacyInformations pharmacyInfo = pharmacy.getOrCreatePharmacyInformationsForAccount(sender.getAccount());

            // Vérifier si c'est la première commande pour ce compte (neverOrdered est true par défaut)
            Boolean newPharmacy = pharmacyInfo.getNeverOrdered();

            // Marquer comme non nouvelle pharmacie maintenant qu'une commande est créée
            pharmacyInfo.setNeverOrdered(false);
            pharmacy.setPharmacyInformations(pharmacyInfo);
            pharmacy = pharmacyService.save(pharmacy);

            return commandService.createCommand(pharmacy, sender, null, body.getCommand(),
                body.getExpedition_date(), newPharmacy);
        }

        return optCommand.get();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected List<PackageEntity> createPackagesInParallel(Command command, SendCommandRequestDTO body) {
        return body.getCommand().getPackages().stream()
            .map(packageDTO -> {
                PackageEntity newPackage = packageService.createPackage(
                    command, 
                    packageDTO, 
                    body.getCommand().getNum_transport()
                );
                packageDTO.setZoneName(newPackage.getZoneName());
                packageDTO.setBarcode(newPackage.getBarcode());
                packageDTO.setCNumTransport(newPackage.getCNumTransport());
                return newPackage;
            })
            .collect(Collectors.toList());
    }

    private SendCommandResponseDTO buildResponse(Command command, List<PackageDTO> packages) {
        SendCommandResponseDTO responseDTO = new SendCommandResponseDTO();
        responseDTO.setId_command(command.getId());
        responseDTO.setMessage("Commande envoyée avec succès");
        responseDTO.setStatus("success");
        responseDTO.setPackages(packages);
        return responseDTO;
    }

    /**
     * Associe automatiquement une commande à une tournée en fonction de la zone
     * Si la pharmacie de la commande a une zone, cherche une tournée avec la même zone pour la date de la commande
     * @param command La commande à associer
     */
    @Transactional(propagation = Propagation.REQUIRED)
    protected void assignCommandToTourByZone(Command command) {
        try {
            // Vérifier que la commande a une pharmacie et une date d'expédition
            if (command.getPharmacy() == null || command.getExpDate() == null) {
                return;
            }

            // Vérifier que la pharmacie a une zone
            if (command.getPharmacy().getZone() == null) {
                log.debug("Pharmacy {} has no zone, skipping tour assignment", command.getPharmacy().getCip());
                return;
            }

            // Vérifier que la zone a un compte
            if (command.getPharmacy().getZone().getAccount() == null) {
                log.debug("Zone {} has no account, skipping tour assignment", command.getPharmacy().getZone().getName());
                return;
            }

            // Extraire la date (sans l'heure) de la date d'expédition
            LocalDate commandDate = command.getExpDate().toLocalDate();

            // Récupérer les tournées pour cette date et ce compte (via la zone)
            Account account = command.getPharmacy().getZone().getAccount();
            List<Tour> tours = tourService.findTours(account, commandDate);

            if (tours.isEmpty()) {
                log.debug("No tours found for date {} and account {}", commandDate, account.getId());
                return;
            }

            // Chercher une tournée avec la même zone que la pharmacie
            Optional<Tour> matchingTour = tours.stream()
                .filter(tour -> tour.getZone() != null)
                .filter(tour -> tour.getZone().getId().equals(command.getPharmacy().getZone().getId()))
                .findFirst();

            if (matchingTour.isPresent()) {
                Tour tour = matchingTour.get();

                // Associer la commande à la tournée
                command.setTour(tour);
                commandService.save(command);

                // Rafraîchir le trajet de la tournée
                refreshTourRoute(tour);

                log.info("Command {} automatically assigned to tour {} based on zone {}",
                    command.getId(), tour.getId(), command.getPharmacy().getZone().getName());
            } else {
                log.debug("No tour found with zone {} for date {}",
                    command.getPharmacy().getZone().getName(), commandDate);
            }

        } catch (Exception e) {
            log.error("Error assigning command to tour by zone", e);
            // Ne pas propager l'erreur pour ne pas bloquer la création de la commande
        }
    }

    /**
     * Rafraîchit le trajet d'une tournée en recalculant sa route via ORS
     * Met à jour la géométrie, la distance estimée et le temps estimé
     * @param tour La tournée dont le trajet doit être rafraîchi
     */
    private void refreshTourRoute(Tour tour) {
        try {
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
            tourRepository.save(tour);
            log.debug("Tour {} route refreshed - distance: {} km, duration: {} mins",
                tour.getId(), tour.getEstimateKm(), tour.getEstimateMins());
        } catch (Exception e) {
            log.error("Error refreshing tour route for tour {}", tour.getId(), e);
            // Ne pas propager l'erreur pour ne pas bloquer l'assignation de la commande
        }
    }
}
