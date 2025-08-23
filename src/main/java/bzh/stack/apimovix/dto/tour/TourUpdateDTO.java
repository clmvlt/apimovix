package bzh.stack.apimovix.dto.tour;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class TourUpdateDTO {
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
} 