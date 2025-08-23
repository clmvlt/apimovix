package bzh.stack.apimovix.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.dto.packageentity.PackageStatusDTO;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {PackageMapper.class, ProfileMapper.class}
)
@Component
public interface PackageMapper {
    PackageMapper INSTANCE = Mappers.getMapper(PackageMapper.class);

    @Autowired
    ProfileMapper profilMapper = Mappers.getMapper(ProfileMapper.class);
    
    @Mapping(target = "command", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    PackageEntity toEntity(PackageDTO dto);

    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    PackageDTO toDto(PackageEntity entity);

    default PackageStatusDTO toPackageStatusDTO(HistoryPackageStatus history) {
        if (history == null) {
            return null;
        }
        PackageStatusDTO dto = new PackageStatusDTO(history.getStatus().getId(), history.getStatus().getName());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setProfil(profilMapper.toDto(history.getProfil()));
        return dto;
    }

    default List<PackageStatusDTO> toPackageStatusDTOList(List<HistoryPackageStatus> histories) {
        if (histories == null) {
            return new ArrayList<>();
        }
        return histories.stream()
                .map(this::toPackageStatusDTO)
                .collect(Collectors.toList());
    }
} 