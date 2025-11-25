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
    uses = {ZoneMapper.class, PharmacyInformationsMapper.class, PharmacyPictureMapper.class}
)
@Component
public interface PharmacyMapper {

    @Named("toDto")
    PharmacyDTO toDto(Pharmacy pharmacy);

    @Named("toDetailDto")
    @Mapping(target = "pictures", source = "pictures")
    PharmacyDetailDTO toDetailDto(Pharmacy pharmacy);

    Pharmacy toEntity(PharmacyUpdateDTO dto);

    @Mapping(target = "cip", ignore = true)
    @Mapping(target = "pictures", ignore = true)
    @Mapping(target = "pharmacyInfos", ignore = true)
    @Mapping(target = "pharmacyInformations", ignore = true)
    @Mapping(target = "name", source = "name")
    Pharmacy updateEntityFromDto(PharmacyUpdateDTO dto, @MappingTarget Pharmacy pharmacy);
}
