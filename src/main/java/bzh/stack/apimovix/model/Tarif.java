package bzh.stack.apimovix.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Table(name = "tarif")
public class Tarif {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "km_max")
    private Double kmMax;
    
    @Column(name = "prix_euro")
    private Double prixEuro;
    
    @ManyToOne
    @JoinColumn(name = "id_account")
    @JsonBackReference
    private Account account;
} 