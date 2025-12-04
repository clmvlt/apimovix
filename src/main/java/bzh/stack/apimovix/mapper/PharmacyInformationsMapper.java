package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.pharmacy.PharmacyUpdateDTO;
import bzh.stack.apimovix.model.PharmacyInformations;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ZoneMapper.class, AccountMapper.class}
)
@Component
public interface PharmacyInformationsMapper {

    @Mapping(target = "cip", ignore = true)
    @Mapping(target = "pharmacy", ignore = true)
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
    @Mapping(target = "commentaire", source = "commentaire")
    @Mapping(target = "neverOrdered", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "account", ignore = true)
    void updateFromDto(PharmacyUpdateDTO dto, @MappingTarget PharmacyInformations pharmacyInformations);
}
