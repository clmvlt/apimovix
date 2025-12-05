package bzh.stack.apimovix.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.TokenNotRequired;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.token.ProfilCreateWithTokenDTO;
import bzh.stack.apimovix.dto.token.ProfileCreationTokenCreateDTO;
import bzh.stack.apimovix.dto.token.ProfileCreationTokenDTO;
import bzh.stack.apimovix.dto.token.TokenValidationResponseDTO;
import bzh.stack.apimovix.mapper.AccountMapper;
import bzh.stack.apimovix.mapper.ProfileMapper;
import bzh.stack.apimovix.mapper.ProfileCreationTokenMapper;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.ProfileCreationToken;
import bzh.stack.apimovix.service.ProfileCreationTokenService;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/profile-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Profile Creation Tokens", description = "API for managing profile creation tokens - HyperAdmin access required")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class ProfileCreationTokenController {

    private final ProfileCreationTokenService profileCreationTokenService;
    private final ProfileCreationTokenMapper tokenMapper;
    private final AccountMapper accountMapper;
    private final ProfileMapper profileMapper;

    @PostMapping
    @HyperAdminRequired
    @Operation(
        summary = "Create a profile creation token (HyperAdmin only)",
        description = "Creates a unique single-use token that allows profile creation for a specific account",
        responses = {
            @ApiResponse(responseCode = "201", description = "Token successfully created",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProfileCreationTokenDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
        }
    )
    public ResponseEntity<?> createToken(
            @Parameter(description = "Token creation data", required = true)
            @Valid @RequestBody ProfileCreationTokenCreateDTO createDTO) {

        ProfileCreationToken token = profileCreationTokenService.createToken(
            createDTO.getAccountId(),
            createDTO.getNotes()
        );

        return MAPIR.created(tokenMapper.toDto(token));
    }

    @GetMapping
    @HyperAdminRequired
    @Operation(
        summary = "Get all tokens (HyperAdmin only)",
        description = "Retrieves a list of all profile creation tokens across all accounts",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of tokens",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = ProfileCreationTokenDTO.class))))
        }
    )
    public ResponseEntity<?> getAllTokens() {
        List<ProfileCreationToken> tokens = profileCreationTokenService.getAllTokens();
        List<ProfileCreationTokenDTO> tokenDTOs = tokens.stream()
                .map(tokenMapper::toDto)
                .collect(Collectors.toList());
        return MAPIR.ok(tokenDTOs);
    }

    @DeleteMapping("/{tokenId}")
    @HyperAdminRequired
    @Operation(
        summary = "Delete a token (HyperAdmin only)",
        description = "Permanently deletes a profile creation token",
        responses = {
            @ApiResponse(responseCode = "204", description = "Token successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token not found", content = @Content)
        }
    )
    public ResponseEntity<?> deleteToken(
            @Parameter(description = "UUID of the token to delete", required = true,
                schema = @Schema(type = "string", format = "uuid"))
            @PathVariable @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") String tokenId) {

        UUID uuid = UUID.fromString(tokenId);
        profileCreationTokenService.deleteToken(uuid);
        return MAPIR.noContent();
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/public/check/{token}")
    @TokenNotRequired
    @Operation(
        summary = "Check if a token is usable (Public)",
        description = "Checks if a token exists and is still available for use (not yet used). Returns token validity status and associated account information. No authentication required.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token check result",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TokenValidationResponseDTO.class)))
        }
    )
    public ResponseEntity<?> checkToken(
            @Parameter(description = "Token to check", required = true)
            @PathVariable String token) {

        Optional<ProfileCreationToken> optToken = profileCreationTokenService.validateToken(token);

        if (optToken.isEmpty()) {
            return MAPIR.ok(TokenValidationResponseDTO.invalid("Token invalide ou déjà utilisé"));
        }

        ProfileCreationToken tokenEntity = optToken.get();
        return MAPIR.ok(TokenValidationResponseDTO.valid(
            tokenEntity.getAccount().getId(),
            accountMapper.toDto(tokenEntity.getAccount())
        ));
    }

    @PostMapping("/public/create-profil")
    @TokenNotRequired
    @Operation(
        summary = "Create a profile with a token (Public)",
        description = "Creates a new user profile using a valid token. The token will be marked as used after successful profile creation. No authentication required.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Profile successfully created",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProfilDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid token or validation error", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists or profile limit reached", content = @Content)
        }
    )
    public ResponseEntity<?> createProfilWithToken(
            @Parameter(description = "Profile creation data with token", required = true)
            @Valid @RequestBody ProfilCreateWithTokenDTO createDTO) {

        try {
            Profil profil = profileCreationTokenService.createProfilWithToken(createDTO);
            return MAPIR.created(profileMapper.toDto(profil));
        } catch (IllegalArgumentException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }
}
