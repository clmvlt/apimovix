package bzh.stack.apimovix.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "profile_creation_token")
public class ProfileCreationToken {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "token", unique = true, nullable = false, length = 64)
    private String token;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonManagedReference
    private Account account;

    @Column(name = "notes")
    private String notes;

    /**
     * Vérifie si le token est valide (non utilisé)
     */
    public boolean isValid() {
        return !isUsed;
    }

    /**
     * Marque le token comme utilisé
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}
