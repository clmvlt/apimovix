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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.TokenNotRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.profil.ForgotPasswordDTO;
import bzh.stack.apimovix.dto.profil.PasswordChangeDTO;
import bzh.stack.apimovix.dto.profil.ProfilCreateDTO;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.profil.ProfilUpdateDTO;
import bzh.stack.apimovix.dto.profil.ResetPasswordDTO;
import bzh.stack.apimovix.mapper.ProfileMapper;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.ProfileService;
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
@RequestMapping(path = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@AdminRequired
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "API for managing user profiles and their permissions")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class ProfileController {

    private final ProfileMapper profileMapper;
    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get all profiles", description = "Retrieves a list of all user profiles for the authenticated user's account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of profiles", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProfilDTO.class)))),
    })
    @TokenRequired
    public ResponseEntity<?> getProfiles(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Profil> profiles = profileService.findProfiles(profil.getAccount());
        List<ProfilDTO> profilDTOs = profiles.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
        return MAPIR.ok(profilDTOs);
    }

    @PostMapping
    @Operation(summary = "Create profile", description = "Creates a new user profile with the specified permissions and settings", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created profile", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProfilDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Profile with similar settings already exists", content = @Content),
    })
    public ResponseEntity<?> createProfile(
            HttpServletRequest request,
            @Parameter(description = "Profile creation data", required = true, schema = @Schema(implementation = ProfilCreateDTO.class)) @Valid @RequestBody ProfilCreateDTO profilCreateDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        Profil createdProfil = profileService.createProfile(profil.getAccount(), profilCreateDTO);
        return MAPIR.created(profileMapper.toDto(createdProfil));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profile", description = "Updates an existing user profile with new permissions and settings (without password)", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated profile", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Profile with similar settings already exists", content = @Content),
    })
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @Parameter(description = "Updated profile data", required = true, schema = @Schema(implementation = ProfilUpdateDTO.class)) @Valid @RequestBody ProfilUpdateDTO profilUpdateDTO,
            @Parameter(description = "UUID of the profile to update", required = true, schema = @Schema(type = "string", format = "uuid")) @PathVariable @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Profil> optProfil = profileService.updateProfilWithoutPassword(profil.getAccount(), profilUpdateDTO, uuid);
        if (optProfil.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the password of the authenticated user's profile", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully changed password", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid password data or passwords do not match", content = @Content),
            @ApiResponse(responseCode = "401", description = "Current password is incorrect", content = @Content),
    })
    @TokenRequired
    public ResponseEntity<?> changePassword(
            HttpServletRequest request,
            @Parameter(description = "Password change data", required = true, schema = @Schema(implementation = PasswordChangeDTO.class)) @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        
        Profil profil = (Profil) request.getAttribute("profil");
        
        // Vérifier que les nouveaux mots de passe correspondent
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            return MAPIR.badRequest("Les nouveaux mots de passe ne correspondent pas");
        }
        
        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordChangeDTO.getCurrentPassword().equals(passwordChangeDTO.getNewPassword())) {
            return MAPIR.badRequest("Le nouveau mot de passe doit être différent de l'ancien");
        }
        
        boolean passwordChanged = profileService.changePassword(profil, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
        
        if (!passwordChanged) {
            return MAPIR.invalidCredentials();
        }
        
        return MAPIR.noContent();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get profile by ID", description = "Retrieves detailed information about a specific user profile", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved profile details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProfilDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content),
    })
    public ResponseEntity<?> getProfil(
            HttpServletRequest request,
            @Parameter(description = "UUID of the profile to retrieve", required = true, schema = @Schema(type = "string", format = "uuid")) @PathVariable @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Profil> optProfil = profileService.findProfile(profil.getAccount(), uuid);
        if (optProfil.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(profileMapper.toDto(optProfil.get()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete profile", description = "Deletes a specific user profile and all its associated data", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted profile", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content),
    })
    public ResponseEntity<?> deleteProfile(
            HttpServletRequest request,
            @Parameter(description = "UUID of the profile to delete", required = true, schema = @Schema(type = "string", format = "uuid")) @PathVariable @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        boolean deleted = profileService.delete(profil.getAccount(), uuid);
        if (!deleted) {
            return MAPIR.notFound();
        }
        return MAPIR.deleted();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a password reset token to the user's email address", responses = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content),
    })
    @TokenNotRequired
    public ResponseEntity<?> forgotPassword(
            @Parameter(description = "Email address for password reset", required = true, schema = @Schema(implementation = ForgotPasswordDTO.class)) @Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        
        profileService.initiatePasswordReset(forgotPasswordDTO.getEmail());
        return MAPIR.noContent();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", description = "Resets the user's password using a valid reset token", responses = {
            @ApiResponse(responseCode = "200", description = "Password successfully reset", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid token or passwords do not match", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token not found or expired", content = @Content),
    })
    @TokenNotRequired
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Password reset data", required = true, schema = @Schema(implementation = ResetPasswordDTO.class)) @Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            return MAPIR.badRequest("Les nouveaux mots de passe ne correspondent pas");
        }
        
        boolean passwordReset = profileService.resetPassword(resetPasswordDTO.getToken(), resetPasswordDTO.getNewPassword());
        
        if (!passwordReset) {
            return MAPIR.badRequest("Token invalide ou expiré");
        }
        
        return MAPIR.noContent();
    }
}