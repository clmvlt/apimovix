package bzh.stack.apimovix.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.command.CommandBasicDTO;
import bzh.stack.apimovix.dto.command.CommandDetailDTO;
import bzh.stack.apimovix.dto.command.CommandExpeditionDTO;
import bzh.stack.apimovix.dto.command.CommandIdsDTO;
import bzh.stack.apimovix.dto.command.CommandSearchDTO;
import bzh.stack.apimovix.dto.command.CommandSearchResponseDTO;
import bzh.stack.apimovix.dto.command.CommandStatusDTO;
import bzh.stack.apimovix.dto.command.CommandUpdateDTO;
import bzh.stack.apimovix.dto.command.CommandUpdateStatusDTO;
import bzh.stack.apimovix.dto.command.CommandUpdateTarifDTO;
import bzh.stack.apimovix.dto.command.CreateCommandRequestDTO;
import bzh.stack.apimovix.dto.common.PictureDTO;
import bzh.stack.apimovix.dto.importer.SendCommandResponseDTO;
import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.mapper.CommandMapper;
import bzh.stack.apimovix.mapper.PackageMapper;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.model.History.HistoryCommandStatus;
import bzh.stack.apimovix.model.Picture.CommandPicture;
import bzh.stack.apimovix.service.SenderService;
import bzh.stack.apimovix.service.command.CommandService;
import bzh.stack.apimovix.service.tour.TourCommandService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import bzh.stack.apimovix.util.PATTERNS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/commands", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Commands", description = "API for managing commands and their lifecycle")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@TokenRequired
public class CommandController {
    private final CommandService commandService;
    private final TourCommandService tourCommandService;
    private final CommandMapper commandMapper;
    private final SenderService senderService;
    private final PackageMapper packageMapper;

    @GetMapping("/by-date/{date}")
    @Operation(summary = "Get commands by date", description = "Retrieves all expedition commands for a specific date", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved commands", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CommandExpeditionDTO.class)))),
    })
    public ResponseEntity<?> getCommandsByDate(
            HttpServletRequest request,
            @Parameter(description = "Date to retrieve commands for (format: yyyy-MM-dd)", required = true, schema = @Schema(type = "string", format = "date")) @Pattern(regexp = PATTERNS.REG_DATE, message = GLOBAL.PATH_INVALID_FORMAT_DATE) @PathVariable String date) {
        Profil profil = (Profil) request.getAttribute("profil");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERNS.DATE);
        LocalDateTime dateTime = LocalDate.parse(date, formatter).atStartOfDay();
        List<CommandExpeditionDTO> commandExpeditionDTOs = commandService.findExpeditionCommands(profil.getAccount(),
                dateTime);
        return MAPIR.ok(commandExpeditionDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get command by ID", description = "Retrieves detailed information about a specific command using its UUID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved command details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommandDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Command not found", content = @Content),
    })
    public ResponseEntity<?> getCommand(
            HttpServletRequest request,
            @Parameter(description = "UUID of the command to retrieve", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Command> optCommand = commandService.findById(profil.getAccount(), uuid);
        if (optCommand.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(commandMapper.toDetailDTO(optCommand.get()));
    }

    @GetMapping("/history/{id}")
    @Operation(summary = "Get command history", description = "Retrieves the complete history of status changes for a specific command", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved command history", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CommandStatusDTO.class)))),
    })
    public ResponseEntity<?> getCommandHistory(
            HttpServletRequest request,
            @Parameter(description = "UUID of the command to retrieve history for", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        List<HistoryCommandStatus> commandHistory = commandService.findCommandHistory(profil.getAccount(), uuid);
        return MAPIR.ok(commandMapper.toCommandStatusDTOList(commandHistory));
    }

    @GetMapping("/pharmacy/{cip}/last-commands")
    @Operation(summary = "Get last 5 commands by pharmacy CIP", description = "Retrieves the last 5 commands for a specific pharmacy using its CIP, filtered by account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved last 5 commands", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CommandBasicDTO.class)))),
    })
    public ResponseEntity<?> getLast5CommandsByPharmacyCip(
            HttpServletRequest request,
            @Parameter(description = "CIP of the pharmacy to retrieve commands for", required = true) @PathVariable String cip) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Command> commands = commandService.findLast5CommandsByPharmacyCip(profil.getAccount(), cip);
        List<CommandBasicDTO> commandDTOs = commands.stream()
                .map(commandMapper::toBasicDTO)
                .collect(Collectors.toList());
        return MAPIR.ok(commandDTOs);
    }

    @PutMapping("/assign/{tourId}")
    @Operation(summary = "Assign commands to tour", description = "Assigns multiple commands to a specific tour", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully assigned commands to tour", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tour or commands not found", content = @Content),
    })
    public ResponseEntity<?> assignCommandsToTour(
            HttpServletRequest request,
            @Parameter(description = "List of command IDs to assign", required = true, schema = @Schema(implementation = CommandIdsDTO.class)) @RequestBody CommandIdsDTO commandIds,
            @Parameter(description = "ID of the tour to assign commands to", required = true) @PathVariable String tourId) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean assigned = tourCommandService.assignCommandsToTour(profil.getAccount(), commandIds, tourId);
        if (!assigned) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @PutMapping("/unassign")
    @Operation(summary = "Unassign commands from tour", description = "Removes multiple commands from their assigned tour", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully unassigned commands from tour", content = @Content),
            @ApiResponse(responseCode = "404", description = "Commands not found", content = @Content),
    })
    public ResponseEntity<?> unassignCommandsFromTour(
            HttpServletRequest request,
            @Parameter(description = "List of command IDs to unassign", required = true, schema = @Schema(implementation = CommandIdsDTO.class)) @RequestBody CommandIdsDTO commandIds) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean unassigned = tourCommandService.unassignCommandsFromTour(profil.getAccount(), commandIds);
        if (!unassigned) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @PutMapping(value = "/state")
    @Operation(summary = "Update command state", description = "Updates the state/status of one or more commands in bulk", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated command state(s)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Command(s) not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> updateCommandState(
            HttpServletRequest request,
            @Parameter(description = "Command status update data", required = true, schema = @Schema(implementation = CommandUpdateStatusDTO.class)) @Valid @RequestBody CommandUpdateStatusDTO commandUpdateStatusDTO) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean updated = commandService.updateCommandStatusBulk(profil, commandUpdateStatusDTO);
        if (!updated) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PutMapping
    @Operation(summary = "Update command state", description = "Updates the expDate of one or more commands in bulk", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated command state(s)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Command(s) not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> updateCommand(
            HttpServletRequest request,
            @Parameter(description = "Command status update data", required = true, schema = @Schema(implementation = CommandUpdateDTO.class)) @Valid @RequestBody CommandUpdateDTO commandUpdateDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        boolean updated = commandService.updateCommandBulk(profil, commandUpdateDTO);
        if (!updated) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PutMapping("/tarif")
    @Operation(summary = "Update command tarif", description = "Updates the tarif of one or more commands in bulk", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated command tarif(s)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Command(s) not found", content = @Content),
    })
    @AdminRequired
    public ResponseEntity<?> updateCommandTarif(
            HttpServletRequest request,
            @Parameter(description = "Command tarif update data", required = true, schema = @Schema(implementation = CommandUpdateTarifDTO.class)) @Valid @RequestBody CommandUpdateTarifDTO commandUpdateTarifDTO) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean updated = commandService.updateCommandTarifBulk(profil, commandUpdateTarifDTO);
        if (!updated) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PostMapping("/{commandId}/picture")
    @MobileRequired
    @Operation(summary = "Upload command picture", description = "Uploads a picture associated with a specific command", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully uploaded command picture", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommandPicture.class))),
            @ApiResponse(responseCode = "404", description = "Command not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while processing picture", content = @Content),
    })
    public ResponseEntity<?> uploadPicture(
            HttpServletRequest request,
            @Parameter(description = "UUID of the command to upload picture for", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String commandId,
            @Parameter(description = "Picture data in base64 format", required = true, schema = @Schema(implementation = PictureDTO.class)) @RequestBody PictureDTO body) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(commandId);
        Optional<Command> command = commandService.findById(profil.getAccount(), uuid);
        if (command.isEmpty()) {
            return MAPIR.notFound();
        }

        CommandPicture picture = commandService.createCommandPicture(command.get(), body.getBase64());

        if (picture == null) {
            return MAPIR.internalServerError();
        }

        return MAPIR.created(picture);
    }

    @PostMapping("/search")
    @Operation(summary = "Search commands", description = "Searches for commands based on provided criteria", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching commands", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CommandSearchResponseDTO.class)))),
    })
    public ResponseEntity<?> searchCommands(
            HttpServletRequest request,
            @Parameter(description = "Search criteria for finding commands", required = true, schema = @Schema(implementation = CommandSearchDTO.class)) @Valid @RequestBody CommandSearchDTO searchDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<CommandSearchResponseDTO> commands = commandService.searchCommands(profil.getAccount(), searchDTO);
        return MAPIR.ok(commands);
    }

    @PostMapping
    @Operation(summary = "Send command", description = "Sends a command to the system for processing", responses = {
            @ApiResponse(responseCode = "200", description = "Command successfully sent", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SendCommandResponseDTO.class))),
    })
    public ResponseEntity<?> sendCommand(HttpServletRequest request,
            @Parameter(description = "Command data to be sent", required = true, schema = @Schema(implementation = CreateCommandRequestDTO.class)) @Valid @RequestBody CreateCommandRequestDTO body) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Sender> senderOptional = senderService.findFirstSenderByAccount(profil.getAccount());
        if (senderOptional.isEmpty()) {
            return MAPIR.notFound();
        }
        Optional<Command> createdCommand = commandService.createCommand(body.getCip(), senderOptional.get(), profil, body.getCommand(), body.getExpedition_date(), false);
        if (createdCommand.isEmpty()) {
            return MAPIR.notFound();
        }
        SendCommandResponseDTO response = new SendCommandResponseDTO();
        response.setId_command(createdCommand.get().getId());
        List<PackageDTO> packageDTOs = createdCommand.get().getPackages().stream()
                .map(packageMapper::toDto)
                .collect(Collectors.toList());
        response.setPackages(packageDTOs);
        response.setStatus("success");
        return MAPIR.ok(response);
    }

    @GetMapping("/hyperadmin/{id}")
    @HyperAdminRequired
    @Operation(summary = "Get command by ID (HyperAdmin)", description = "Retrieves detailed information about any command without account restriction (HyperAdmin only)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved command details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommandDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Command not found", content = @Content),
    })
    public ResponseEntity<?> getCommandHyperAdmin(
            HttpServletRequest request,
            @Parameter(description = "UUID of the command to retrieve", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Optional<Command> optCommand = commandService.findByIdHyperAdmin(uuid);
        if (optCommand.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(commandMapper.toDetailDTO(optCommand.get()));
    }

    @DeleteMapping("/{id}")
    @HyperAdminRequired
    @Operation(summary = "Delete command", description = "Deletes a command and all its associated packages (HyperAdmin only)", responses = {
            @ApiResponse(responseCode = "204", description = "Command successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Command not found", content = @Content),
    })
    public ResponseEntity<?> deleteCommand(
            HttpServletRequest request,
            @Parameter(description = "UUID of the command to delete", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        boolean deleted = commandService.deleteCommandHyperAdmin(uuid);
        if (!deleted) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }
}
