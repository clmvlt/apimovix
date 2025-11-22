package bzh.stack.apimovix.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "importer_token")
public class ImporterToken {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(name = "is_beta_proxy", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBetaProxy = false;

    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime lastUsedAt;
}
