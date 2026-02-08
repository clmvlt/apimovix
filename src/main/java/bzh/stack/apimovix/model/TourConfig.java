package bzh.stack.apimovix.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Configuration de tournée pour un compte
 * Définit les paramètres d'une tournée récurrente
 */
@Entity
@Table(name = "tour_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 100)
    private String tourName;

    @Column(length = 7)
    private String tourColor; // Format: #RRGGBB

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profil_id")
    private Profil profil;

    /**
     * Récurrence sous forme de bit flags pour les jours de la semaine
     * Bit 0 = Lundi, Bit 1 = Mardi, ... Bit 6 = Dimanche
     * Ex: 0b0011111 = Lundi à Vendredi (31)
     * Ex: 0b1000001 = Lundi et Dimanche (65)
     */
    @Column(nullable = false)
    private Integer recurrence;

    /**
     * Heure de creation automatique de la tournee (par defaut 08:00)
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalTime tourHour = LocalTime.of(8, 0);

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods pour gérer la récurrence
    public boolean isActiveOnMonday() {
        return (recurrence & 0b0000001) != 0;
    }

    public boolean isActiveOnTuesday() {
        return (recurrence & 0b0000010) != 0;
    }

    public boolean isActiveOnWednesday() {
        return (recurrence & 0b0000100) != 0;
    }

    public boolean isActiveOnThursday() {
        return (recurrence & 0b0001000) != 0;
    }

    public boolean isActiveOnFriday() {
        return (recurrence & 0b0010000) != 0;
    }

    public boolean isActiveOnSaturday() {
        return (recurrence & 0b0100000) != 0;
    }

    public boolean isActiveOnSunday() {
        return (recurrence & 0b1000000) != 0;
    }
}
