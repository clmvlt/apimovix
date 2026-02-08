package bzh.stack.apimovix.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.anomalie.AnomalieCreateDTO;
import bzh.stack.apimovix.dto.anomalie.AnomalieDTO;
import bzh.stack.apimovix.dto.anomalie.AnomalieDetailDTO;
import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.model.Picture.AnomaliePicture;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ProfileMapper.class, PharmacyMapper.class, PackageMapper.class, CommandMapper.class}
)
@Component
public interface AnomalieMapper {

    @Mapping(target = "commandId", source = "command.id")
    AnomalieDTO toDto(Anomalie anomalie);

    @Mapping(target = "pictures", expression = "java(mapPictures(anomalie.getPictures()))")
    @Mapping(target = "commandId", source = "command.id")
    @Mapping(target = "command", source = "command")
    AnomalieDetailDTO toDetailDto(Anomalie anomalie);
    
    Anomalie toEntity(AnomalieDTO dto);
    void updateAnomalieFromCreateDTO(AnomalieCreateDTO dto, @MappingTarget Anomalie anomalie);

    default List<AnomalieDTO> toAnomalieDTOsList(List<Anomalie> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    default List<AnomaliePicture> mapPictures(List<AnomaliePicture> pictures) {
        if (pictures == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(pictures);
    }
}
