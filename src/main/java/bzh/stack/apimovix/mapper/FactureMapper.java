package bzh.stack.apimovix.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.facture.FactureDTO;
import bzh.stack.apimovix.model.Facture;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Component
public interface FactureMapper {

    @Mapping(target = "accountId", source = "account.id")
    FactureDTO toDto(Facture facture);

    @AfterMapping
    default void setPdfUrl(Facture facture, @MappingTarget FactureDTO dto) {
        dto.setPdfUrl(facture.getPdfUrl());
    }
}
