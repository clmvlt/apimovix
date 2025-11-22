package bzh.stack.apimovix.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import bzh.stack.apimovix.util.UrlUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "facture",
       indexes = {
           @Index(name = "idx_facture_account", columnList = "id_account"),
           @Index(name = "idx_facture_date", columnList = "date_facture DESC")
       })
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "date_facture")
    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDateTime dateFacture;

    @Column(name = "montant_ttc", precision = 10, scale = 2)
    private BigDecimal montantTTC;

    @Column(name = "is_paid", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPaid = false;

    @Column(name = "created_at")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_account", nullable = false)
    @JsonBackReference
    private Account account;

    public String getPdfUrl() {
        if (pdfPath == null) {
            return null;
        }
        return UrlUtil.getBaseUrl() + "/factures/pdf/" + id;
    }
}
