package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;

public interface ProfileRepository extends JpaRepository<Profil, UUID> {
    @Query("SELECT p FROM Profil p JOIN FETCH p.account WHERE p.email = :email AND (p.deleted IS NULL OR p.deleted = false)")
    public Optional<Profil> findByEmail(String email);
    
    @Query("SELECT p FROM Profil p JOIN FETCH p.account WHERE p.identifiant = :identifiant AND (p.deleted IS NULL OR p.deleted = false)")
    public Optional<Profil> findByIdentifiant(String identifiant);
    
    @Query("SELECT p FROM Profil p JOIN FETCH p.account WHERE p.token = :token AND (p.deleted IS NULL OR p.deleted = false)")
    public Optional<Profil> findByToken(String token);

    @Query("SELECT p FROM Profil p JOIN FETCH p.account WHERE p.account = :account AND (p.deleted IS NULL OR p.deleted = false) ORDER BY p.createdAt DESC")
    public List<Profil> findProfiles(@Param("account") Account account);

    @Query("SELECT p FROM Profil p JOIN FETCH p.account WHERE p.id = :id AND p.account = :account AND (p.deleted IS NULL OR p.deleted = false)")
    public Profil findProfile(@Param("account") Account account, @Param("id") UUID id);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profil p WHERE p.email = :email AND (p.deleted IS NULL OR p.deleted = false)")
    public boolean existsByEmail(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profil p WHERE p.identifiant = :identifiant AND (p.deleted IS NULL OR p.deleted = false)")
    public boolean existsByIdentifiant(@Param("identifiant") String identifiant);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profil p WHERE p.email = :email AND p.id != :id AND (p.deleted IS NULL OR p.deleted = false)")
    public boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profil p WHERE p.identifiant = :identifiant AND p.id != :id AND (p.deleted IS NULL OR p.deleted = false)")
    public boolean existsByIdentifiantAndIdNot(@Param("identifiant") String identifiant, @Param("id") UUID id);
} 