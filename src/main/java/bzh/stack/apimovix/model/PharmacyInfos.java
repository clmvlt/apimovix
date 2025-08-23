package bzh.stack.apimovix.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.model.Picture.PharmacyInfosPicture;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "pharmacy_infos")
public class PharmacyInfos {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
    
    @Column(name = "invalid_geocodage")
    private Boolean invalidGeocodage;
    
    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "id_account")
    @JsonBackReference
    private Account account;
    
    @ManyToOne
    @JoinColumn(name = "cip")
    @JsonBackReference
    private Pharmacy pharmacy;
    
    @ManyToOne
    @JoinColumn(name = "id_profil")
    @JsonBackReference
    private Profil profil;
    
    @OneToMany(mappedBy = "pharmacyInfos")
    @JsonManagedReference
    private List<PharmacyInfosPicture> pictures = new ArrayList<>();
} 