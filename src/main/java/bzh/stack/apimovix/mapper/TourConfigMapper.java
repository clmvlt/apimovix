package bzh.stack.apimovix.mapper;

import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.tourconfig.RecurrenceDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigCreateDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigDetailDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigUpdateDTO;
import bzh.stack.apimovix.dto.zone.ZoneDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.TourConfig;
import bzh.stack.apimovix.model.Zone;
import org.mapstruct.*;

/**
 * Mapper pour convertir entre les entités TourConfig et leurs DTOs
 */
@Mapper(componentModel = "spring")
public interface TourConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", source = "accountId")
    @Mapping(target = "zone", source = "zone", qualifiedByName = "zoneDTOToZone")
    @Mapping(target = "profil", source = "profil", qualifiedByName = "profilDTOToProfil")
    @Mapping(target = "recurrence", source = "recurrence", qualifiedByName = "recurrenceDTOToInteger")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TourConfig toEntity(TourConfigCreateDTO dto);

    @Mapping(target = "account", source = "account", qualifiedByName = "accountToAccountDTO")
    @Mapping(target = "zone", source = "zone", qualifiedByName = "zoneToZoneDTO")
    @Mapping(target = "profil", source = "profil", qualifiedByName = "profilToProfilDTO")
    @Mapping(target = "recurrence", source = "recurrence", qualifiedByName = "integerToRecurrenceDTO")
    TourConfigDetailDTO toDetailDTO(TourConfig entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", source = "accountId")
    @Mapping(target = "zone", source = "zone", qualifiedByName = "zoneDTOToZone")
    @Mapping(target = "profil", source = "profil", qualifiedByName = "profilDTOToProfil")
    @Mapping(target = "recurrence", source = "recurrence", qualifiedByName = "recurrenceDTOToInteger")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(TourConfigUpdateDTO dto, @MappingTarget TourConfig entity);

    @Named("recurrenceDTOToInteger")
    default Integer recurrenceDTOToInteger(RecurrenceDTO recurrenceDTO) {
        return recurrenceDTO != null ? recurrenceDTO.toInteger() : 0;
    }

    @Named("integerToRecurrenceDTO")
    default RecurrenceDTO integerToRecurrenceDTO(Integer recurrence) {
        return RecurrenceDTO.fromInteger(recurrence);
    }

    // Conversion UUID vers Account
    default Account map(java.util.UUID accountId) {
        if (accountId == null) {
            return null;
        }
        Account account = new Account();
        account.setId(accountId);
        return account;
    }

    // Conversion ZoneDTO vers Zone
    @Named("zoneDTOToZone")
    default Zone zoneDTOToZone(ZoneDTO zoneDTO) {
        if (zoneDTO == null || zoneDTO.getId() == null) {
            return null;
        }
        Zone zone = new Zone();
        zone.setId(zoneDTO.getId());
        zone.setName(zoneDTO.getName());
        return zone;
    }

    // Conversion Zone vers ZoneDTO
    @Named("zoneToZoneDTO")
    default ZoneDTO zoneToZoneDTO(Zone zone) {
        if (zone == null) {
            return null;
        }
        ZoneDTO zoneDTO = new ZoneDTO();
        zoneDTO.setId(zone.getId());
        zoneDTO.setName(zone.getName());
        return zoneDTO;
    }

    // Conversion ProfilDTO vers Profil
    @Named("profilDTOToProfil")
    default Profil profilDTOToProfil(ProfilDTO profilDTO) {
        if (profilDTO == null || profilDTO.getId() == null) {
            return null;
        }
        Profil profil = new Profil();
        profil.setId(profilDTO.getId());
        profil.setIdentifiant(profilDTO.getIdentifiant());
        return profil;
    }

    // Conversion Profil vers ProfilDTO
    @Named("profilToProfilDTO")
    default ProfilDTO profilToProfilDTO(Profil profil) {
        if (profil == null) {
            return null;
        }
        ProfilDTO profilDTO = new ProfilDTO();
        profilDTO.setId(profil.getId());
        profilDTO.setIdentifiant(profil.getIdentifiant());
        profilDTO.setFirstName(profil.getFirstName());
        profilDTO.setLastName(profil.getLastName());
        profilDTO.setEmail(profil.getEmail());
        profilDTO.setIsActive(profil.getIsActive());
        profilDTO.setIsAdmin(profil.getIsAdmin());
        return profilDTO;
    }

    // Conversion Account vers AccountDTO
    @Named("accountToAccountDTO")
    default bzh.stack.apimovix.dto.account.AccountDTO accountToAccountDTO(Account account) {
        if (account == null) {
            return null;
        }
        bzh.stack.apimovix.dto.account.AccountDTO accountDTO = new bzh.stack.apimovix.dto.account.AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setSociete(account.getSociete());
        accountDTO.setAddress1(account.getAddress1());
        accountDTO.setAddress2(account.getAddress2());
        accountDTO.setPostalCode(account.getPostalCode());
        accountDTO.setCity(account.getCity());
        accountDTO.setCountry(account.getCountry());
        accountDTO.setIsActive(account.getIsActive());
        return accountDTO;
    }
}
