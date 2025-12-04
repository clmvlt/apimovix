package bzh.stack.apimovix.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.model.Picture.AccountLogo;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "account")
public class Account {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "societe")
    private String societe;
    
    @Column(name = "address1")
    private String address1;
    
    @Column(name = "address2")
    private String address2;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @CreationTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime updatedAt;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "max_profiles", columnDefinition = "INT DEFAULT 0")
    private Integer maxProfiles = 0;
    
    @Column(name = "anomalies_emails")
    private String anomaliesEmails;
    
    // Configuration SMTP
    @Column(name = "smtp_host")
    private String smtpHost;
    
    @Column(name = "smtp_port")
    private Integer smtpPort;
    
    @Column(name = "smtp_username")
    private String smtpUsername;
    
    @Column(name = "smtp_password")
    private String smtpPassword;
    
    @Column(name = "smtp_enable", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smtpEnable = false;
    
    @Column(name = "smtp_use_tls", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean smtpUseTls = true;
    
    @Column(name = "smtp_use_ssl", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smtpUseSsl = false;
    
    @Column(name = "is_scan_cip", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isScanCIP = false;

    @Column(name = "auto_send_anomalie_emails", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean autoSendAnomalieEmails = false;

    @OneToMany(mappedBy = "account")
    @JsonBackReference
    private List<Profil> profils = new ArrayList<>();

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private AccountLogo logo;
} 