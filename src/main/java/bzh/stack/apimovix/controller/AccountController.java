package bzh.stack.apimovix.controller;

import java.util.UUID;

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
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.account.AccountCreateDTO;
import bzh.stack.apimovix.dto.account.AccountDTO;
import bzh.stack.apimovix.dto.account.AccountDetailDTO;
import bzh.stack.apimovix.dto.account.AccountUpdateDTO;
import bzh.stack.apimovix.mapper.AccountMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.AccountService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@TokenRequired
@RequestMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Account", description = "API for account management")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping("/details")
    @Operation(summary = "Get account details", description = "Gets detailed information about the connected user's account including SMTP configuration (admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDetailDTO.class)))
    })
    @AdminRequired
    public ResponseEntity<?> getAccountDetails(HttpServletRequest request) {
        
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            Account account = accountService.findAccountById(profil.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
            AccountDetailDTO accountDetailDTO = accountMapper.toDetailDto(account);
            return MAPIR.ok(accountDetailDTO);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Update connected account", description = "Updates the properties of the connected user's account (admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated account", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDTO.class)))
    })
    @AdminRequired
    public ResponseEntity<?> updateAccount(
            @Parameter(description = "Account update data", required = true, schema = @Schema(implementation = AccountUpdateDTO.class)) @RequestBody AccountUpdateDTO updateDTO,
            HttpServletRequest request) {
        
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            Account updatedAccount = accountService.updateAccount(profil.getAccount().getId(), updateDTO);
            AccountDTO accountDTO = accountMapper.toDto(updatedAccount);
            return MAPIR.ok(accountDTO);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @GetMapping("/test-hyperadmin")
    @Operation(summary = "Test HyperAdmin token", description = "Tests if the provided token has HyperAdmin privileges", responses = {
            @ApiResponse(responseCode = "200", description = "Token is valid and has HyperAdmin privileges")
    })
    @HyperAdminRequired
    public ResponseEntity<?> testHyperAdminToken() {
        return MAPIR.ok("Token HyperAdmin valide");
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Gets an account by its ID (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDTO.class)))
    })
    @HyperAdminRequired
    public ResponseEntity<?> getAccountById(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID accountId) {

        try {
            Account account = accountService.getAccountById(accountId);
            AccountDTO accountDTO = accountMapper.toDto(account);
            return MAPIR.ok(accountDTO);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all accounts", description = "Gets all accounts (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all accounts")
    })
    @HyperAdminRequired
    public ResponseEntity<?> getAllAccounts() {

        try {
            var accounts = accountService.getAllAccounts();
            var accountDTOs = accounts.stream()
                .map(accountMapper::toDto)
                .toList();
            return MAPIR.ok(accountDTOs);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Create account", description = "Creates a new account (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully created account", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDTO.class)))
    })
    @HyperAdminRequired
    public ResponseEntity<?> createAccount(
            @Parameter(description = "Account creation data", required = true, schema = @Schema(implementation = AccountCreateDTO.class)) @RequestBody AccountCreateDTO createDTO) {

        try {
            Account newAccount = accountService.createAccount(createDTO);
            AccountDTO accountDTO = accountMapper.toDto(newAccount);
            return MAPIR.ok(accountDTO);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @PutMapping("/update/{accountId}")
    @Operation(summary = "Update account by ID", description = "Updates an account by its ID (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated account", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDTO.class)))
    })
    @HyperAdminRequired
    public ResponseEntity<?> updateAccountById(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID accountId,
            @Parameter(description = "Account update data", required = true, schema = @Schema(implementation = AccountUpdateDTO.class)) @RequestBody AccountUpdateDTO updateDTO) {

        try {
            Account updatedAccount = accountService.updateAccountById(accountId, updateDTO);
            AccountDTO accountDTO = accountMapper.toDto(updatedAccount);
            return MAPIR.ok(accountDTO);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{accountId}")
    @Operation(summary = "Delete account", description = "Deletes an account by its ID (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted account")
    })
    @HyperAdminRequired
    public ResponseEntity<?> deleteAccount(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID accountId) {

        try {
            accountService.deleteAccount(accountId);
            return MAPIR.ok("Compte supprimé avec succès");
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }
} 