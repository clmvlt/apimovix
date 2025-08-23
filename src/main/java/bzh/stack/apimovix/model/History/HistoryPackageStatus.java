package bzh.stack.apimovix.model.History;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.StatusType.PackageStatus;
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
@ToString(exclude = "packageEntity")
@Entity
@Table(name = "history_package_status")
public class HistoryPackageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_status")
    @JsonInclude(value = Include.ALWAYS)
    private PackageStatus status;
    
    @ManyToOne
    @JoinColumn(name = "barcode")
    @JsonBackReference
    private PackageEntity packageEntity;
    
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
        HistoryPackageStatus that = (HistoryPackageStatus) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 