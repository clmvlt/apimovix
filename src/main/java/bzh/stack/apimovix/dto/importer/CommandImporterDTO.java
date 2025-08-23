package bzh.stack.apimovix.dto.importer;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import lombok.Data;

@Data
public class CommandImporterDTO {
    private String num_transport;
    private List<PackageDTO> packages = new ArrayList<>();
    private Integer number_of_packages;
    private Double volume;
    private Double weight;
    private String close_date;
} 