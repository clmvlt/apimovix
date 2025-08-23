package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.zone.ZoneDTO;
import bzh.stack.apimovix.dto.zone.ZoneDetailDTO;
import bzh.stack.apimovix.model.Zone;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {PharmacyMapper.class}
)
@Component
public interface ZoneMapper {
    
    @Named("toDto")
    ZoneDTO toDto(Zone zone);

    @Named("toDetailDto")
    @Mapping(target = "pharmacies", source = "pharmacies", qualifiedByName = "toDetailDto")
    ZoneDetailDTO toDetailDto(Zone zone);
    
    Zone toEntity(ZoneDTO dto);
    
}
