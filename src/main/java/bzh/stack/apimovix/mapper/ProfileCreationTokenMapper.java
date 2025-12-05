package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.token.ProfileCreationTokenDTO;
import bzh.stack.apimovix.model.ProfileCreationToken;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {AccountMapper.class}
)
@Component
public interface ProfileCreationTokenMapper {

    ProfileCreationTokenDTO toDto(ProfileCreationToken token);

    ProfileCreationToken toEntity(ProfileCreationTokenDTO dto);
}
