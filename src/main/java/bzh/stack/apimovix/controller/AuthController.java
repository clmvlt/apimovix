package bzh.stack.apimovix.controller;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.dto.auth.LoginRequestDTO;
import bzh.stack.apimovix.dto.profil.ProfilAuthDTO;
import bzh.stack.apimovix.mapper.ProfileMapper;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.AuthService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "API for user authentication and profile management")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ProfileMapper profilMapper;

    @PostMapping("/login")
    @Operation(summary = "User authentication", description = "Authenticates a user with their credentials and returns their profile information with an authentication token", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated user", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProfilAuthDTO.class)))
    })
    public ResponseEntity<?> login(
            @Parameter(description = "User credentials for authentication", required = true, schema = @Schema(implementation = LoginRequestDTO.class)) @Valid @RequestBody LoginRequestDTO creds) {
        Optional<Profil> profilOpt = authService.login(creds);

        if (profilOpt.isPresent()) {
            ProfilAuthDTO profilAuthDTO = profilMapper.toAuthDto(profilOpt.get());
            return MAPIR.ok(profilAuthDTO);
        }

        return MAPIR.invalidCredentials();
    }

    @GetMapping("/me")
    @Operation(summary = "Get connected profile", description = "Retrieves the information of the currently authenticated user's profile", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProfilAuthDTO.class)))
    })
    @MobileRequired
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null) {
            return MAPIR.invalidCredentials();
        }

        ProfilAuthDTO profilAuthDTO = profilMapper.toAuthDto(profil);
        return MAPIR.ok(profilAuthDTO);
    }
}