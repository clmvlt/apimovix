package bzh.stack.apimovix.repository.pharmacy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Pharmacy;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, String> {
    
    @Query("SELECT DISTINCT p FROM Pharmacy p LEFT JOIN FETCH p.pictures WHERE p.account IS NOT NULL")
    List<Pharmacy> findPharmacies();

    @Query("SELECT DISTINCT p FROM Pharmacy p LEFT JOIN FETCH p.pictures WHERE p.account.id = :accountId")
    List<Pharmacy> findPharmaciesByAccount(@Param("accountId") java.util.UUID accountId);

    @Query("SELECT p FROM Pharmacy p LEFT JOIN FETCH p.pictures WHERE p.cip = :cip AND p.account IS NOT NULL")
    Pharmacy findPharmacy(@Param("cip") String cip);

    @Query("SELECT p FROM Pharmacy p LEFT JOIN FETCH p.pictures WHERE p.cip = :cip AND p.account.id = :accountId")
    Pharmacy findPharmacyByAccount(@Param("cip") String cip, @Param("accountId") java.util.UUID accountId);

    @Query("SELECT p FROM Pharmacy p LEFT JOIN FETCH p.pictures WHERE p.cip = :cip")
    Pharmacy findPharmacyByCipOnly(@Param("cip") String cip);

    @Query(value = "SELECT DISTINCT p.* FROM pharmacy p " +
           "LEFT JOIN pharmacy_picture pp ON p.cip = pp.cip " +
           "WHERE p.id_account IS NOT NULL " +
           "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:city IS NULL OR " +
           "    (LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%')) OR " +
           "     (:cityAlias IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :cityAlias, '%'))))) " +
           "AND (:postalCode IS NULL OR LOWER(p.postal_code) LIKE LOWER(CONCAT('%', :postalCode, '%'))) " +
           "AND (:cip IS NULL OR LOWER(p.cip) LIKE LOWER(CONCAT('%', :cip, '%'))) " +
           "AND (:address IS NULL OR " +
           "    LOWER(p.address1) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address2) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address3) LIKE LOWER(CONCAT('%', :address, '%'))) " +
           "AND (:isLocationValid IS NULL OR " +
           "    (:isLocationValid = true AND p.latitude IS NOT NULL AND p.latitude != 0 AND p.longitude IS NOT NULL AND p.longitude != 0) OR " +
           "    (:isLocationValid = false AND (p.latitude IS NULL OR p.latitude = 0 OR p.longitude IS NULL OR p.longitude = 0))) " +
           "AND (:zoneId IS NULL OR " +
           "    (:zoneId = 'none' AND p.id_zone IS NULL) OR " +
           "    (:zoneId IS NOT NULL AND :zoneId != 'none' AND CAST(p.id_zone AS VARCHAR) = :zoneId)) " +
           "AND (:hasOrdered IS NULL OR " +
           "    (:hasOrdered = true AND (p.never_ordered IS NULL OR p.never_ordered = false)) OR " +
           "    (:hasOrdered = false AND p.never_ordered = true))" +
           " ORDER BY p.cip LIMIT :maxResults",
           nativeQuery = true)
    List<Pharmacy> searchPharmacies(
        @Param("name") String name,
        @Param("city") String city,
        @Param("cityAlias") String cityAlias,
        @Param("postalCode") String postalCode,
        @Param("cip") String cip,
        @Param("address") String address,
        @Param("isLocationValid") Boolean isLocationValid,
        @Param("maxResults") Integer maxResults,
        @Param("zoneId") String zoneId,
        @Param("hasOrdered") Boolean hasOrdered
    );

    @Query(value = "SELECT DISTINCT p.* FROM pharmacy p " +
           "LEFT JOIN pharmacy_picture pp ON p.cip = pp.cip " +
           "WHERE CAST(p.id_account AS VARCHAR) = :accountId " +
           "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:city IS NULL OR " +
           "    (LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%')) OR " +
           "     (:cityAlias IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :cityAlias, '%'))))) " +
           "AND (:postalCode IS NULL OR LOWER(p.postal_code) LIKE LOWER(CONCAT('%', :postalCode, '%'))) " +
           "AND (:cip IS NULL OR LOWER(p.cip) LIKE LOWER(CONCAT('%', :cip, '%'))) " +
           "AND (:address IS NULL OR " +
           "    LOWER(p.address1) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address2) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address3) LIKE LOWER(CONCAT('%', :address, '%'))) " +
           "AND (:isLocationValid IS NULL OR " +
           "    (:isLocationValid = true AND p.latitude IS NOT NULL AND p.latitude != 0 AND p.longitude IS NOT NULL AND p.longitude != 0) OR " +
           "    (:isLocationValid = false AND (p.latitude IS NULL OR p.latitude = 0 OR p.longitude IS NULL OR p.longitude = 0))) " +
           "AND (:zoneId IS NULL OR " +
           "    (:zoneId = 'none' AND p.id_zone IS NULL) OR " +
           "    (:zoneId IS NOT NULL AND :zoneId != 'none' AND CAST(p.id_zone AS VARCHAR) = :zoneId)) " +
           "AND (:hasOrdered IS NULL OR " +
           "    (:hasOrdered = true AND (p.never_ordered IS NULL OR p.never_ordered = false)) OR " +
           "    (:hasOrdered = false AND p.never_ordered = true))" +
           " ORDER BY p.cip LIMIT :maxResults",
           nativeQuery = true)
    List<Pharmacy> searchPharmaciesByAccount(
        @Param("accountId") String accountId,
        @Param("name") String name,
        @Param("city") String city,
        @Param("cityAlias") String cityAlias,
        @Param("postalCode") String postalCode,
        @Param("cip") String cip,
        @Param("address") String address,
        @Param("isLocationValid") Boolean isLocationValid,
        @Param("maxResults") Integer maxResults,
        @Param("zoneId") String zoneId,
        @Param("hasOrdered") Boolean hasOrdered
    );

    boolean existsByCip(String cip);
} 