package bzh.stack.apimovix.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.dto.importer.CommandImporterDTO;
import bzh.stack.apimovix.model.History.HistoryCommandStatus;
import bzh.stack.apimovix.model.Picture.CommandPicture;
import bzh.stack.apimovix.util.PATTERNS;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"lastHistoryStatus", "tour", "pictures", "packages"})
@Entity
@Table(name = "command")
public class Command {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .appendPattern("XXX")
        .toFormatter();

    public Command() {
        this.packages = new ArrayList<>();
        this.pictures = new ArrayList<>();
    }
    
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @Column(name = "tour_order")
    private Integer tourOrder;

    @Column(name = "exp_date")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime expDate;

    @Column(name = "comment")
    private String comment;

    @Column(name = "new_pharmacy")
    private Boolean newPharmacy = false;

    @Column(name = "l_latitude")
    private Double latitude;

    @Column(name = "l_longitude")
    private Double longitude;

    @Column(name = "tarif")
    private Double tarif;

    @Column(name = "is_forced")
    private Boolean isForced = false;

    @ManyToOne
    @JoinColumn(name = "id_tour")
    @JsonBackReference
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "cip")
    private Pharmacy pharmacy;

    @ManyToOne
    @JoinColumn(name = "code")
    private Sender sender;

    @ManyToOne()
    @JoinColumn(name = "id_status")
    @JsonManagedReference
    private HistoryCommandStatus lastHistoryStatus;

    @OneToMany(mappedBy = "command", fetch = FetchType.EAGER)
    @JsonManagedReference
    @BatchSize(size = 50)
    private List<CommandPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "command", fetch = FetchType.EAGER)
    @JsonManagedReference
    @BatchSize(size = 50)
    private List<PackageEntity> packages = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(id, command.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void mapFromDTO(CommandImporterDTO commandDTO) {
        if (commandDTO.getClose_date() != null && commandDTO.getClose_date() != "") {
            this.closeDate = LocalDateTime.parse(commandDTO.getClose_date(), DATE_TIME_FORMATTER);
        }
    }
} 