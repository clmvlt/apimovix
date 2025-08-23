package bzh.stack.apimovix.model.History;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.StatusType.CommandStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@ToString(exclude = "command")
@Entity
@Table(name = "history_command_status")
public class HistoryCommandStatus {
    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_status")
    @JsonInclude(value = Include.ALWAYS)
    private CommandStatus status;
    
    @ManyToOne
    @JoinColumn(name = "id_command")
    @JsonBackReference
    private Command command;
    
    @Column(name = "created_at")
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
        HistoryCommandStatus that = (HistoryCommandStatus) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 