package bzh.stack.apimovix.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.model.History.HistoryTourStatus;
import bzh.stack.apimovix.util.PATTERNS;
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
@ToString(exclude = {"lastHistoryStatus", "commands"})
@Entity
@Table(name = "tour")
public class Tour {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "immat")
    private String immat;

    @Column(name = "start_km")
    private Integer startKm;

    @Column(name = "end_km")
    private Integer endKm;

    @Column(name = "initial_date")
    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDate initialDate;

    @Column(name = "start_date")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime endDate;

    @Column(name = "color")
    private String color;

    @Column(name = "estimate_mins")
    private Double estimateMins;

    @Column(name = "estimate_km")
    private Double estimateKm;

    @Column(name = "geometry", columnDefinition = "TEXT")
    private String geometry;

    @ManyToOne
    @JoinColumn(name = "id_account")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "id_profil")
    private Profil profil;

    @ManyToOne
    @JoinColumn(name = "id_status")
    private HistoryTourStatus lastHistoryStatus;

    @OneToMany(mappedBy = "tour", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Command> commands = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tour tour = (Tour) o;
        return Objects.equals(id, tour.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getFormattedDate() {
        if (initialDate == null) {
            return "";
        }
        
        String[] mois = {"janvier", "février", "mars", "avril", "mai", "juin", 
                        "juillet", "août", "septembre", "octobre", "novembre", "décembre"};
        
        int jour = initialDate.getDayOfMonth();
        int moisIndex = initialDate.getMonthValue() - 1;
        int annee = initialDate.getYear();
        
        StringBuilder formattedDate = new StringBuilder();
        formattedDate.append(jour).append(" ").append(mois[moisIndex]).append(" ").append(annee);
        
        return formattedDate.toString();
    }
}