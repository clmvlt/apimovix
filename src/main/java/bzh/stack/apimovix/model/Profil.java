package bzh.stack.apimovix.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.util.PATTERNS;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "profil")
public class Profil {
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "identifiant")
    private String identifiant;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Column(name = "token")
    private String token;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_admin")
    private Boolean isAdmin;
    
    @Column(name = "is_web")
    private Boolean isWeb;
    
    @Column(name = "is_mobile")
    private Boolean isMobile;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "is_stock")
    private Boolean isStock;
    
    @Column(name = "is_avtrans")
    private Boolean isAvtrans;
    
    @ManyToOne
    @JoinColumn(name = "id_account")
    @JsonManagedReference
    private Account account;
    
    @Column(name = "deleted")
    private Boolean deleted = false;

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName.trim());
        }
        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName.trim());
        }
        return fullName.length() > 0 ? fullName.toString() : null;
    }
} 