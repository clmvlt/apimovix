package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.importertoken.ImporterTokenCreateDTO;
import bzh.stack.apimovix.dto.importertoken.ImporterTokenDTO;
import bzh.stack.apimovix.dto.importertoken.ImporterTokenUpdateDTO;
import bzh.stack.apimovix.model.ImporterToken;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Component
public interface ImporterTokenMapper {

    ImporterTokenDTO toDto(ImporterToken token);

    ImporterToken toEntity(ImporterTokenCreateDTO dto);

    void updateEntityFromDto(ImporterTokenUpdateDTO dto, @MappingTarget ImporterToken token);
}
