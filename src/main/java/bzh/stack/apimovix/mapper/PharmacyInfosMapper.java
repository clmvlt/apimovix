package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.pharmacyinfos.PharmacyInfosCreateDTO;
import bzh.stack.apimovix.dto.pharmacyinfos.PharmacyInfosDTO;
import bzh.stack.apimovix.model.PharmacyInfos;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {PharmacyMapper.class, ProfileMapper.class}
)
@Component
public interface PharmacyInfosMapper {
    @Mapping(target = "pharmacy", source = "pharmacy")
    @Mapping(target = "profil", source = "profil")
    PharmacyInfosDTO toDto(PharmacyInfos pharmacyInfos);

    void updateEntityFromCreateDto(PharmacyInfosCreateDTO dto, @MappingTarget PharmacyInfos pharmacyInfos);
}
