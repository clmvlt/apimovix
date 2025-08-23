package bzh.stack.apimovix.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.importer.SendCommandRequestDTO;
import bzh.stack.apimovix.dto.importer.SendCommandResponseDTO;
import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.service.command.CommandService;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
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

    @Transactional
    public SendCommandResponseDTO sendCommand(@Valid SendCommandRequestDTO body) {
        try {
            CompletableFuture<Sender> senderFuture = CompletableFuture.supplyAsync(() -> {
                Optional<Sender> optSender = senderService.findSender(body.getSender().getCode());
                return optSender.orElseGet(() -> senderService.createSender(body.getSender()));
            });

            CompletableFuture<Pharmacy> pharmacyFuture = CompletableFuture.supplyAsync(() -> {
                Optional<Pharmacy> optPharmacy = pharmacyService.findPharmacy(body.getRecipient().getCip());
                return optPharmacy.orElseGet(() -> pharmacyService.createPharmacy(body.getRecipient()));
            });

            Sender sender = senderFuture.get();
            Pharmacy pharmacy = pharmacyFuture.get();

            Command command = createOrUpdateCommand(pharmacy, sender, body);

            List<PackageEntity> packages = createPackagesInParallel(command, body);

            command.setPackages(packages);
            command = commandService.save(command);

            return buildResponse(command, body.getCommand().getPackages());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du traitement de la commande", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Command createOrUpdateCommand(Pharmacy pharmacy, Sender sender, SendCommandRequestDTO body) {
        Optional<Command> optCommand = commandService.findPharmacyCommandByDate(pharmacy.getCip(), body.getExpedition_date());
        
        if (optCommand.isEmpty()) {
            Boolean newPharmacy = pharmacy.getNeverOrdered();
            if (pharmacy.getNeverOrdered()) {
                pharmacy.setNeverOrdered(false);
                pharmacy = pharmacyService.save(pharmacy);
            }
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
}
