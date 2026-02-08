package bzh.stack.apimovix.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import bzh.stack.apimovix.dto.command.CommandBasicDTO;
import bzh.stack.apimovix.dto.command.CommandDTO;
import bzh.stack.apimovix.dto.command.CommandDetailDTO;
import bzh.stack.apimovix.dto.command.CommandExpeditionDTO;
import bzh.stack.apimovix.dto.command.CommandSimpleDTO;
import bzh.stack.apimovix.dto.command.CommandStatusDTO;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.History.HistoryCommandStatus;

@Mapper(
    componentModel = "spring",
    uses = {PackageMapper.class, ProfileMapper.class},
    unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE
)
public interface CommandMapper {
    
    CommandMapper INSTANCE = Mappers.getMapper(CommandMapper.class);

    @Autowired
    ProfileMapper profilMapper = Mappers.getMapper(ProfileMapper.class);

    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "pharmacy", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "lastHistoryStatus", ignore = true)
    @Mapping(target = "pictures", ignore = true)
    @Mapping(target = "packages", ignore = true)
    Command toEntity(CommandDTO dto);

    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    @Mapping(target = "pharmacyCommentaire", expression = "java(entity.getPharmacy() != null ? entity.getPharmacy().getCommentaire() : null)")
    @Mapping(target = "tourColor", source = "tour.color")
    @Named("toDTO")
    CommandDTO toDTO(Command entity);

    @Named("toBasicDTO")
    CommandBasicDTO toBasicDTO(Command entity);

    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    @Named("toSimpleDTO")
    CommandSimpleDTO toSimpleDTO(Command entity);

    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    @Mapping(target = "tourColor", source = "tour.color")
    @Mapping(target = "pharmacyCommentaire", expression = "java(entity.getPharmacy() != null ? entity.getPharmacy().getCommentaire() : null)")
    CommandDetailDTO toDetailDTO(Command entity);

    @Mapping(target = "status.id", source = "lastHistoryStatus.status.id")
    @Mapping(target = "status.name", source = "lastHistoryStatus.status.name")
    @Mapping(target = "packagesNumber", expression = "java(entity.getPackages() != null ? entity.getPackages().size() : 0)")
    @Mapping(target = "pharmacyCommentaire", expression = "java(entity.getPharmacy() != null ? entity.getPharmacy().getCommentaire() : null)")
    @Mapping(target = "totalWeight", expression = "java(entity.getPackages() != null ? entity.getPackages().stream().map(p -> p.getWeight() != null ? p.getWeight() : 0.0).reduce(0.0, Double::sum).floatValue() : 0.0f)")
    CommandExpeditionDTO toExpeditionDTO(Command entity);
    
    default List<CommandExpeditionDTO> toExpeditionDTOList(List<Command> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toExpeditionDTO)
                .collect(Collectors.toList());
    }

    default List<CommandDTO> toDTOList(List<Command> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    default List<CommandBasicDTO> toBasicDTOList(List<Command> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toBasicDTO)
                .collect(Collectors.toList());
    }

    default CommandStatusDTO toCommandStatusDTO(HistoryCommandStatus history) {
        if (history == null) {
            return null;
        }
        CommandStatusDTO dto = new CommandStatusDTO(history.getStatus());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setProfil(profilMapper.toDto(history.getProfil()));
        return dto;
    }

    default List<CommandStatusDTO> toCommandStatusDTOList(List<HistoryCommandStatus> histories) {
        if (histories == null) {
            return new ArrayList<>();
        }
        return histories.stream()
                .map(this::toCommandStatusDTO)
                .collect(Collectors.toList());
    }
} 