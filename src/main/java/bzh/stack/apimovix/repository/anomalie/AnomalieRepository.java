package bzh.stack.apimovix.repository.anomalie;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Anomalie;

@Repository
public interface AnomalieRepository extends JpaRepository<Anomalie, UUID> {

    @Query("SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.pharmacy p " +
           "LEFT JOIN FETCH a.profil pr " +
           "LEFT JOIN FETCH a.typeAnomalie t " +
           "WHERE a.account = :account " +
           "ORDER BY a.createdAt DESC " +
           "LIMIT 100")
    List<Anomalie> findAnomalies(@Param("account") Account account);
    
    @Query(value = "SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.pharmacy p " +
           "LEFT JOIN FETCH a.profil pr " +
           "LEFT JOIN FETCH a.typeAnomalie t " +
           "WHERE a.account = :account",
           countQuery = "SELECT COUNT(DISTINCT a) FROM Anomalie a WHERE a.account = :account")
    Page<Anomalie> findAnomaliesPaginated(@Param("account") Account account, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.packages " +
           "LEFT JOIN FETCH a.pharmacy " +
           "LEFT JOIN FETCH a.profil " +
           "LEFT JOIN FETCH a.typeAnomalie " +
           "LEFT JOIN FETCH a.command c " +
           "LEFT JOIN FETCH c.lastHistoryStatus lhs " +
           "LEFT JOIN FETCH lhs.status " +
           "WHERE a.account = :account AND a.id = :id")
    Anomalie findAnomalie(@Param("account") Account account, @Param("id") UUID id);
    
    @Query("SELECT a FROM Anomalie a WHERE a.pharmacy.cip = :cip")
    List<Anomalie> findAllAnomaliesByPharmacyCip(@Param("cip") String cip);

    // Recherche detaillee sans dates
    @Query("SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.pharmacy p " +
           "LEFT JOIN FETCH a.profil pr " +
           "LEFT JOIN FETCH a.typeAnomalie t " +
           "WHERE a.account = :account " +
           "AND (a.pharmacy IS NULL OR LENGTH(a.pharmacy.cip) > 5) " +
           "AND (:userId IS NULL OR a.profil.id = :userId) " +
           "AND (:cip IS NULL OR a.pharmacy.cip = :cip) " +
           "AND (:typeCode IS NULL OR a.typeAnomalie.code = :typeCode) " +
           "ORDER BY a.createdAt DESC LIMIT :limit OFFSET :offset")
    List<Anomalie> searchAnomalies(
        @Param("account") Account account,
        @Param("userId") UUID userId,
        @Param("cip") String cip,
        @Param("typeCode") String typeCode,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );

    // Recherche detaillee avec dates
    @Query("SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.pharmacy p " +
           "LEFT JOIN FETCH a.profil pr " +
           "LEFT JOIN FETCH a.typeAnomalie t " +
           "WHERE a.account = :account " +
           "AND (a.pharmacy IS NULL OR LENGTH(a.pharmacy.cip) > 5) " +
           "AND (:userId IS NULL OR a.profil.id = :userId) " +
           "AND a.createdAt >= :dateDebut " +
           "AND a.createdAt <= :dateFin " +
           "AND (:cip IS NULL OR a.pharmacy.cip = :cip) " +
           "AND (:typeCode IS NULL OR a.typeAnomalie.code = :typeCode) " +
           "ORDER BY a.createdAt DESC LIMIT :limit OFFSET :offset")
    List<Anomalie> searchAnomaliesWithDateRange(
        @Param("account") Account account,
        @Param("userId") UUID userId,
        @Param("dateDebut") LocalDateTime dateDebut,
        @Param("dateFin") LocalDateTime dateFin,
        @Param("cip") String cip,
        @Param("typeCode") String typeCode,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );

    // Recherche globale sans dates (multi-mots) avec override PharmacyInformations
    @Query("SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.pharmacy p " +
           "LEFT JOIN FETCH a.profil pr " +
           "LEFT JOIN FETCH a.typeAnomalie t " +
           "LEFT JOIN p.pharmacyInformationsList pi WITH pi.account = :account " +
           "WHERE a.account = :account " +
           "AND (a.pharmacy IS NULL OR LENGTH(a.pharmacy.cip) > 5) " +
           "AND (:userId IS NULL OR a.profil.id = :userId) " +
           "AND (:typeCode IS NULL OR a.typeAnomalie.code = :typeCode) " +
           "AND (:q1 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q1%) " +
           "AND (:q2 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q2%) " +
           "AND (:q3 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q3%) " +
           "AND (:q4 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q4%) " +
           "AND (:q5 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q5%) " +
           "ORDER BY a.createdAt DESC LIMIT :limit OFFSET :offset")
    List<Anomalie> searchAnomaliesGlobal(
        @Param("account") Account account,
        @Param("userId") UUID userId,
        @Param("typeCode") String typeCode,
        @Param("q1") String q1,
        @Param("q2") String q2,
        @Param("q3") String q3,
        @Param("q4") String q4,
        @Param("q5") String q5,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );

    // Recherche globale avec dates (multi-mots) avec override PharmacyInformations
    @Query("SELECT DISTINCT a FROM Anomalie a " +
           "LEFT JOIN FETCH a.pharmacy p " +
           "LEFT JOIN FETCH a.profil pr " +
           "LEFT JOIN FETCH a.typeAnomalie t " +
           "LEFT JOIN p.pharmacyInformationsList pi WITH pi.account = :account " +
           "WHERE a.account = :account " +
           "AND (a.pharmacy IS NULL OR LENGTH(a.pharmacy.cip) > 5) " +
           "AND (:userId IS NULL OR a.profil.id = :userId) " +
           "AND a.createdAt >= :dateDebut " +
           "AND a.createdAt <= :dateFin " +
           "AND (:typeCode IS NULL OR a.typeAnomalie.code = :typeCode) " +
           "AND (:q1 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q1%) " +
           "AND (:q2 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q2%) " +
           "AND (:q3 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q3%) " +
           "AND (:q4 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q4%) " +
           "AND (:q5 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, a.pharmacy.name, ''), ' ', COALESCE(a.pharmacy.cip, ''), ' ', COALESCE(pi.city, a.pharmacy.city, ''), ' ', COALESCE(pi.postalCode, a.pharmacy.postalCode, ''), ' ', COALESCE(pi.address1, a.pharmacy.address1, ''), ' ', COALESCE(pi.address2, a.pharmacy.address2, ''), ' ', COALESCE(pi.address3, a.pharmacy.address3, ''))) LIKE %:q5%) " +
           "ORDER BY a.createdAt DESC LIMIT :limit OFFSET :offset")
    List<Anomalie> searchAnomaliesGlobalWithDateRange(
        @Param("account") Account account,
        @Param("userId") UUID userId,
        @Param("dateDebut") LocalDateTime dateDebut,
        @Param("dateFin") LocalDateTime dateFin,
        @Param("typeCode") String typeCode,
        @Param("q1") String q1,
        @Param("q2") String q2,
        @Param("q3") String q3,
        @Param("q4") String q4,
        @Param("q5") String q5,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset
    );

    @Query("SELECT a FROM Anomalie a WHERE a.command.id = :commandId")
    List<Anomalie> findByCommandId(@Param("commandId") UUID commandId);
} 