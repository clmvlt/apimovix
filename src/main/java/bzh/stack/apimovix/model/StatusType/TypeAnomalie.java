package bzh.stack.apimovix.model.StatusType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "type_anomalie")
public class TypeAnomalie {
    
    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;
} 