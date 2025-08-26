package bzh.stack.apimovix.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.model.Picture.AnomaliePicture;
import bzh.stack.apimovix.model.StatusType.TypeAnomalie;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;

@Data
@Entity
@Table(name = "anomalie", 
       indexes = {
           @Index(name = "idx_anomalie_account_created", columnList = "id_account, created_at DESC"),
           @Index(name = "idx_anomalie_pharmacy", columnList = "cip"),
           @Index(name = "idx_anomalie_profil", columnList = "id_profil"),
           @Index(name = "idx_anomalie_type", columnList = "code"),
           @Index(name = "idx_anomalie_created", columnList = "created_at DESC")
       })
public class Anomalie {
    
    @Id
    private UUID id;

    @Column(name = "other")
    private String other;

    @Column(name = "actions")
    private String actions;

    @Column(name = "created_at")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "cip")
    @JsonBackReference
    private Pharmacy pharmacy;

    @ManyToOne
    @JoinColumn(name = "id_account", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "code")
    private TypeAnomalie typeAnomalie;

    @ManyToOne
    @JoinColumn(name = "id_profil", nullable = false)
    private Profil profil;

    @OneToMany(mappedBy = "anomalie", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<AnomaliePicture> pictures = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "anomalie_package",
        joinColumns = @JoinColumn(name = "id_anomalie"),
        inverseJoinColumns = @JoinColumn(name = "barcode")
    )
    @JsonManagedReference
    private List<PackageEntity> packages = new ArrayList<>();
} 