package bzh.stack.apimovix.dto.tourconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour gérer la récurrence des tournées par jour de la semaine
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tour recurrence configuration by day of week")
public class RecurrenceDTO {

    @Schema(description = "Tour is active on Mondays", example = "true")
    private Boolean monday;

    @Schema(description = "Tour is active on Tuesdays", example = "true")
    private Boolean tuesday;

    @Schema(description = "Tour is active on Wednesdays", example = "true")
    private Boolean wednesday;

    @Schema(description = "Tour is active on Thursdays", example = "true")
    private Boolean thursday;

    @Schema(description = "Tour is active on Fridays", example = "true")
    private Boolean friday;

    @Schema(description = "Tour is active on Saturdays", example = "false")
    private Boolean saturday;

    @Schema(description = "Tour is active on Sundays", example = "false")
    private Boolean sunday;

    /**
     * Convertit le DTO en valeur entière avec bit flags
     * Bit 0 = Lundi, Bit 1 = Mardi, ... Bit 6 = Dimanche
     */
    public Integer toInteger() {
        int value = 0;
        if (Boolean.TRUE.equals(monday)) value |= 0b0000001;
        if (Boolean.TRUE.equals(tuesday)) value |= 0b0000010;
        if (Boolean.TRUE.equals(wednesday)) value |= 0b0000100;
        if (Boolean.TRUE.equals(thursday)) value |= 0b0001000;
        if (Boolean.TRUE.equals(friday)) value |= 0b0010000;
        if (Boolean.TRUE.equals(saturday)) value |= 0b0100000;
        if (Boolean.TRUE.equals(sunday)) value |= 0b1000000;
        return value;
    }

    /**
     * Crée un DTO à partir d'une valeur entière avec bit flags
     */
    public static RecurrenceDTO fromInteger(Integer value) {
        if (value == null) {
            return new RecurrenceDTO();
        }
        return RecurrenceDTO.builder()
                .monday((value & 0b0000001) != 0)
                .tuesday((value & 0b0000010) != 0)
                .wednesday((value & 0b0000100) != 0)
                .thursday((value & 0b0001000) != 0)
                .friday((value & 0b0010000) != 0)
                .saturday((value & 0b0100000) != 0)
                .sunday((value & 0b1000000) != 0)
                .build();
    }
}
