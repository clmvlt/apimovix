package bzh.stack.apimovix.model.History;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.StatusType.TourStatus;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "tour")
@Entity
@Table(name = "history_tour_status")
public class HistoryTourStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_status")
    @JsonInclude(value = Include.ALWAYS)
    private TourStatus status;
    
    @ManyToOne
    @JoinColumn(name = "id_tour")
    @JsonBackReference
    private Tour tour;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        }
    }
    
    @ManyToOne
    @JoinColumn(name = "id_profil")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Profil profil;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryTourStatus that = (HistoryTourStatus) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 