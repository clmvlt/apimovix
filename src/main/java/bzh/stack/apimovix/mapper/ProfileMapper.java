package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.profil.ProfilAuthDTO;
import bzh.stack.apimovix.dto.profil.ProfilCreateDTO;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.profil.ProfilUpdateDTO;
import bzh.stack.apimovix.model.Profil;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {AccountMapper.class}
)
@Component
public interface ProfileMapper {

    @Named("toDto")
    @Mapping(target = "profilPicture", expression = "java(toProfilPictureUrl(profil))")
    ProfilDTO toDto(Profil profil);

    @Named("toAuthDto")
    @Mapping(target = "profilPicture", expression = "java(toProfilPictureUrl(profil))")
    ProfilAuthDTO toAuthDto(Profil profil);

    default String toProfilPictureUrl(Profil profil) {
        if (profil == null || profil.getProfilPicture() == null || profil.getProfilPicture().trim().isEmpty()) {
            return null;
        }
        // Convert path like "profil/{profilId}/{imageId}.jpg" to "images/profil/{profilId}/{imageId}.jpg"
        return "images/" + profil.getProfilPicture().replace("\\", "/");
    }
    
    Profil toEntity(ProfilDTO dto);
    
    Profil toEntity(ProfilAuthDTO dto);
    
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "profilPicture", ignore = true)
    void updateEntityFromCreateDto(ProfilCreateDTO dto, @MappingTarget Profil profil);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "profilPicture", ignore = true)
    void updateEntityFromUpdateDto(ProfilUpdateDTO dto, @MappingTarget Profil profil);
}
