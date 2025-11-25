package bzh.stack.apimovix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import bzh.stack.apimovix.dto.pharmacy.PharmacyPictureDTO;
import bzh.stack.apimovix.model.Picture.PharmacyPicture;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@Component
public interface PharmacyPictureMapper {

    @Mapping(target = "imagePath", expression = "java(picture.getImagePath())")
    PharmacyPictureDTO toDto(PharmacyPicture picture);
}
