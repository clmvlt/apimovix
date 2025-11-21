package bzh.stack.apimovix.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.sender.SenderCreateDTO;
import bzh.stack.apimovix.dto.sender.SenderResponseDTO;
import bzh.stack.apimovix.dto.sender.SenderUpdateDTO;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.service.SenderService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@TokenRequired
@RequestMapping(value = "/senders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Senders", description = "API for managing senders (HyperAdmin only)")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class SenderController {
    private final SenderService senderService;

    @HyperAdminRequired
    @GetMapping
    @Operation(summary = "Get all senders (HyperAdmin only)", description = "Retrieves a list of all senders in the system. This operation requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of senders", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SenderResponseDTO.class)))),
    })
    public ResponseEntity<?> getAllSenders() {
        List<Sender> senders = senderService.findAllSenders();
        List<SenderResponseDTO> senderDTOs = senders.stream()
                .map(this::toResponseDTO)
                .toList();
        return MAPIR.ok(senderDTOs);
    }

    @HyperAdminRequired
    @GetMapping("/{code}")
    @Operation(summary = "Get sender by code (HyperAdmin only)", description = "Retrieves detailed information about a specific sender using its code. This operation requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sender details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SenderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sender not found", content = @Content),
    })
    public ResponseEntity<?> getSender(
            @Parameter(description = "Code of the sender to retrieve", required = true) @PathVariable String code) {
        Optional<Sender> sender = senderService.findSender(code);
        if (sender.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(toResponseDTO(sender.get()));
    }

    @HyperAdminRequired
    @PostMapping
    @Operation(summary = "Create sender (HyperAdmin only)", description = "Creates a new sender with the provided information. This operation requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created sender", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SenderResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or sender already exists", content = @Content),
    })
    public ResponseEntity<?> createSender(
            @Parameter(description = "Sender creation data", required = true, schema = @Schema(implementation = SenderCreateDTO.class)) @Valid @RequestBody SenderCreateDTO senderCreateDTO) {
        try {
            Sender sender = senderService.createSenderFromDTO(senderCreateDTO);
            return MAPIR.created(toResponseDTO(sender));
        } catch (IllegalArgumentException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @HyperAdminRequired
    @PutMapping("/{code}")
    @Operation(summary = "Update sender (HyperAdmin only)", description = "Updates the information of an existing sender. This operation requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated sender", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SenderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sender not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
    })
    public ResponseEntity<?> updateSender(
            @Parameter(description = "Code of the sender to update", required = true) @PathVariable String code,
            @Parameter(description = "Updated sender information", required = true, schema = @Schema(implementation = SenderUpdateDTO.class)) @RequestBody SenderUpdateDTO senderUpdateDTO) {
        try {
            Optional<Sender> optSender = senderService.updateSender(code, senderUpdateDTO);
            if (optSender.isEmpty()) {
                return MAPIR.notFound();
            }
            return MAPIR.ok(toResponseDTO(optSender.get()));
        } catch (IllegalArgumentException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @HyperAdminRequired
    @DeleteMapping("/{code}")
    @Operation(summary = "Delete sender (HyperAdmin only)", description = "Deletes a specific sender. This operation requires HyperAdmin privileges. WARNING: This may affect existing commands linked to this sender.", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted sender", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sender not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting sender", content = @Content)
    })
    public ResponseEntity<?> deleteSender(
            @Parameter(description = "Code of the sender to delete", required = true) @PathVariable String code) {
        try {
            boolean deleted = senderService.deleteSender(code);
            if (!deleted) {
                return MAPIR.notFound();
            }
            return MAPIR.deleted();
        } catch (Exception e) {
            return MAPIR.internalServerError();
        }
    }

    private SenderResponseDTO toResponseDTO(Sender sender) {
        SenderResponseDTO dto = new SenderResponseDTO();
        dto.setCode(sender.getCode());
        dto.setName(sender.getName());
        dto.setAddress1(sender.getAddress1());
        dto.setAddress2(sender.getAddress2());
        dto.setAddress3(sender.getAddress3());
        dto.setPostalCode(sender.getPostalCode());
        dto.setCity(sender.getCity());
        dto.setCountry(sender.getCountry());
        dto.setInformations(sender.getInformations());
        dto.setQuality(sender.getQuality());
        dto.setFirstName(sender.getFirstName());
        dto.setLastName(sender.getLastName());
        dto.setEmail(sender.getEmail());
        dto.setPhone(sender.getPhone());
        dto.setFax(sender.getFax());
        dto.setLatitude(sender.getLatitude());
        dto.setLongitude(sender.getLongitude());
        dto.setAccountId(sender.getAccount() != null ? sender.getAccount().getId().toString() : null);
        dto.setAccountName(sender.getAccount() != null ? sender.getAccount().getSociete() : null);
        return dto;
    }
}
