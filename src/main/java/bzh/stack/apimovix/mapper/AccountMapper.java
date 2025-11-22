package bzh.stack.apimovix.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.account.AccountDTO;
import bzh.stack.apimovix.dto.account.AccountDetailDTO;
import bzh.stack.apimovix.model.Account;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Component
public interface AccountMapper {

    AccountDTO toDto(Account account);

    AccountDetailDTO toDetailDto(Account account);

    Account toEntity(AccountDTO dto);

    @AfterMapping
    default void setLogoUrl(Account account, @MappingTarget AccountDTO dto) {
        if (account.getLogo() != null) {
            dto.setLogoUrl(account.getLogo().getImagePath());
        }
    }

    @AfterMapping
    default void setLogoUrlForDetail(Account account, @MappingTarget AccountDetailDTO dto) {
        if (account.getLogo() != null) {
            dto.setLogoUrl(account.getLogo().getImagePath());
        }
    }

}
