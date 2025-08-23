package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyDetailDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyUpdateDTO;
import bzh.stack.apimovix.model.Pharmacy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ZoneMapper.class}
)
@Component
public interface PharmacyMapper {
    
    @Named("toDto")
    PharmacyDTO toDto(Pharmacy pharmacy);
    
    @Named("toDetailDto")
    PharmacyDetailDTO toDetailDto(Pharmacy pharmacy);
    
    Pharmacy toEntity(PharmacyUpdateDTO dto);

    @Mapping(target = "cip", ignore = true)
    @Mapping(target = "pictures", ignore = true)
    @Mapping(target = "pharmacyInfos", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "address1", source = "address1")
    @Mapping(target = "address2", source = "address2")
    @Mapping(target = "address3", source = "address3")
    @Mapping(target = "postalCode", source = "postal_code")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "informations", source = "informations")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "fax", source = "fax")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "quality", source = "quality")
    @Mapping(target = "firstName", source = "first_name")
    @Mapping(target = "lastName", source = "last_name")
    @Mapping(target = "neverOrdered", ignore = true)
    Pharmacy updateEntityFromDto(PharmacyUpdateDTO dto, @MappingTarget Pharmacy pharmacy);
}
