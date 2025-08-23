package bzh.stack.apimovix.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import bzh.stack.apimovix.model.History.HistoryPackageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"lastHistoryStatus", "command"})
@Entity
@Table(name = "package")
public class PackageEntity {

    @Id
    @Column(name = "barcode")
    private String barcode;

    @Column(name = "id")
    private String id = "";

    @Column(name = "type")
    private String type = "default";

    @Column(name = "designation")
    private String designation = "";

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "weight")
    private Double weight = 0.0;

    @Column(name = "volume")
    private Double volume = 0.0;

    @Column(name = "length")
    private Double length = 0.0;

    @Column(name = "width")
    private Double width = 0.0;

    @Column(name = "height")
    private Double height = 0.0;

    @Column(name = "fresh")
    private Boolean fresh = false;

    @Column(name = "num")
    private String num = "";

    @Column(name = "num_transport")
    private String cNumTransport = "N/A";

    @Column(name = "zone_name")
    private String zoneName = "00";

    @ManyToOne
    @JoinColumn(name = "id_command", nullable = false)
    @JsonBackReference
    private Command command;

    @ManyToOne
    @JoinColumn(name = "id_status")
    private HistoryPackageStatus lastHistoryStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageEntity that = (PackageEntity) o;
        return Objects.equals(barcode, that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode);
    }
} 