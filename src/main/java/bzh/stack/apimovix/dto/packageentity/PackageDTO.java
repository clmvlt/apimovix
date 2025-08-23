package bzh.stack.apimovix.dto.packageentity;

import lombok.Data;

@Data
public class PackageDTO {
    private String type;
    private String designation;
    private Integer quantity;
    private String id;
    private Double weight;
    private Double volume;
    private Double length;
    private Double width;
    private Double height;
    private Boolean fresh;
    private String num;
    private String zoneName;
    private String cNumTransport;
    private String barcode;
    private String labelUrl;

    private PackageStatusDTO status;
} 