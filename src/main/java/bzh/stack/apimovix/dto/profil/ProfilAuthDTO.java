package bzh.stack.apimovix.dto.profil;

import bzh.stack.apimovix.dto.account.AccountDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProfilAuthDTO extends ProfilDTO {
    private String token;
    private String passwordHash;
    private AccountDTO account;
}
