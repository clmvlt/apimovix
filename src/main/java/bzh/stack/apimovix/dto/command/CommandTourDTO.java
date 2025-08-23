package bzh.stack.apimovix.dto.command;

import bzh.stack.apimovix.model.Tour;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandTourDTO {
    private String id;
    private String name;
    private String color;

    public CommandTourDTO(Tour tour) {
        this.id = tour.getId();
        this.name = tour.getName();
        this.color = tour.getColor();
    }

    public CommandTourDTO(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
