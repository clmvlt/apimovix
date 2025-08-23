package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
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
    
}
