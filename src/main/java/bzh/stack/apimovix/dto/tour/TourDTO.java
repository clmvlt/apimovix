package bzh.stack.apimovix.dto.tour;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.command.CommandDTO;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.zone.ZoneDTO;
import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class TourDTO {
    private String id;
    private String name;
    private String immat;
    private Integer startKm;
    private Integer endKm;

    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDate initialDate;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime startDate;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime endDate;

    private String color;
    private Double estimateMins;
    private Double estimateKm;
    private String geometry;

    private ProfilDTO profil;
    private TourStatusDTO status;
    private ZoneDTO zone;
    private List<CommandDTO> commands = new java.util.ArrayList<>();
}
