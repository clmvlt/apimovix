package bzh.stack.apimovix.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
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
} 