package bzh.stack.apimovix.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.tour.TourCreateDTO;
import bzh.stack.apimovix.dto.tour.TourDTO;
import bzh.stack.apimovix.dto.tour.TourDetailDTO;
import bzh.stack.apimovix.dto.tour.TourStatusDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateDTO;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.History.HistoryTourStatus;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommandMapper.class, ProfileMapper.class}
)
@Component
public interface TourMapper {
    
    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    @Mapping(target = "status.profil", source = "lastHistoryStatus.profil", qualifiedByName = "toDto")
    @Mapping(target = "profil", source = "profil", qualifiedByName = "toDto")
    @Mapping(target = "commands", source = "commands", qualifiedByName = "toDTO")
    TourDTO toDto(Tour tour);
    
    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    @Mapping(target = "status.profil", source = "lastHistoryStatus.profil", qualifiedByName = "toDto")
    @Mapping(target = "profil", source = "profil", qualifiedByName = "toDto")
    TourDetailDTO toDetailDto(Tour tour);
    
    @Mapping(target = "id", source = "status.id")
    @Mapping(target = "name", source = "status.name")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "profil", source = "profil", qualifiedByName = "toDto")
    TourStatusDTO toTourStatusDTO(HistoryTourStatus historyTourStatus);
    
    List<TourStatusDTO> toTourStatusDTOList(List<HistoryTourStatus> historyTourStatusList);
    
    @Mapping(target = "commands", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    Tour toEntity(TourDTO dto);
    
    @Mapping(target = "commands", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    Tour toCreateEntity(TourCreateDTO dto);
    
    @Mapping(target = "commands", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    Tour toEntity(TourUpdateDTO dto);
    
    @Mapping(target = "commands", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    void updateEntityFromDto(TourDTO dto, @MappingTarget Tour tour);
    
    @Mapping(target = "commands", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    void updateEntityFromUpdateDto(TourUpdateDTO dto, @MappingTarget Tour tour);
} 