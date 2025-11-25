package bzh.stack.apimovix.model.Picture;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.model.PharmacyInfos;
import bzh.stack.apimovix.util.PATTERNS;
import bzh.stack.apimovix.util.UrlUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "pharmacy_infos_picture")
public class PharmacyInfosPicture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_pharmacy_infos")
    private PharmacyInfos pharmacyInfos;

    public String getImagePath() {
        return UrlUtil.getBaseUrl() + "/images/" + name;
    }
} 