package bzh.stack.apimovix.repository.pharmacy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Pharmacy;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, String> {

    @Query("SELECT DISTINCT p FROM Pharmacy p")
    List<Pharmacy> findAllPharmaciesBase();

    @Query("SELECT DISTINCT p FROM Pharmacy p LEFT JOIN FETCH p.pharmacyInformationsList WHERE p IN :pharmacies")
    List<Pharmacy> fetchInformationsForPharmacies(@Param("pharmacies") List<Pharmacy> pharmacies);

    default List<Pharmacy> findPharmacies(java.util.UUID accountId) {
        // Fetch all pharmacies first
        List<Pharmacy> pharmacies = findAllPharmaciesBase();
        if (!pharmacies.isEmpty()) {
            // Fetch informations
            pharmacies = fetchInformationsForPharmacies(pharmacies);
        }
        return pharmacies;
    }

    default List<Pharmacy> findPharmaciesByAccount(java.util.UUID accountId) {
        // Fetch all pharmacies first
        List<Pharmacy> pharmacies = findAllPharmaciesBase();
        if (!pharmacies.isEmpty()) {
            // Fetch informations
            pharmacies = fetchInformationsForPharmacies(pharmacies);
            // Load the correct PharmacyInformations for each pharmacy
            if (accountId != null) {
                pharmacies.forEach(p -> p.loadPharmacyInformationsForAccount(accountId));
            }
        }
        return pharmacies;
    }

    @Query("SELECT p FROM Pharmacy p WHERE p.cip = :cip")
    Pharmacy findPharmacyBase(@Param("cip") String cip);

    @Query("SELECT p FROM Pharmacy p LEFT JOIN FETCH p.pharmacyInformationsList WHERE p.cip = :cip")
    Pharmacy fetchInformationsForPharmacy(@Param("cip") String cip);

    default Pharmacy findPharmacy(String cip, java.util.UUID accountId) {
        Pharmacy pharmacy = findPharmacyBase(cip);
        if (pharmacy != null) {
            pharmacy = fetchInformationsForPharmacy(cip);
        }
        return pharmacy;
    }

    default Pharmacy findPharmacyByAccount(String cip, java.util.UUID accountId) {
        Pharmacy pharmacy = findPharmacy(cip, accountId);
        if (pharmacy != null && accountId != null) {
            pharmacy.loadPharmacyInformationsForAccount(accountId);
        }
        return pharmacy;
    }

    @Query("SELECT p FROM Pharmacy p WHERE p.cip = :cip")
    Pharmacy findPharmacyByCipOnly(@Param("cip") String cip);

    @Query(value = "SELECT p.cip FROM pharmacy p " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:postalCode IS NULL OR LOWER(p.postal_code) LIKE LOWER(CONCAT('%', :postalCode, '%'))) " +
           "AND (:cip IS NULL OR LOWER(p.cip) LIKE LOWER(CONCAT('%', :cip, '%'))) " +
           "AND (:address IS NULL OR " +
           "    LOWER(p.address1) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address2) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address3) LIKE LOWER(CONCAT('%', :address, '%'))) " +
           "AND (:isLocationValid IS NULL OR " +
           "    (:isLocationValid = true AND p.latitude IS NOT NULL AND p.latitude != 0 AND p.longitude IS NOT NULL AND p.longitude != 0) OR " +
           "    (:isLocationValid = false AND (p.latitude IS NULL OR p.latitude = 0 OR p.longitude IS NULL OR p.longitude = 0))) " +
           " ORDER BY p.cip LIMIT :maxResults",
           nativeQuery = true)
    List<String> searchPharmaciesCips(
        @Param("name") String name,
        @Param("city") String city,
        @Param("postalCode") String postalCode,
        @Param("cip") String cip,
        @Param("address") String address,
        @Param("isLocationValid") Boolean isLocationValid,
        @Param("maxResults") Integer maxResults
    );

    @Query("SELECT DISTINCT p FROM Pharmacy p WHERE p.cip IN :cips ORDER BY p.cip")
    List<Pharmacy> findPharmaciesByCipsBase(@Param("cips") List<String> cips);

    @Query("SELECT DISTINCT p FROM Pharmacy p LEFT JOIN FETCH p.pharmacyInformationsList WHERE p.cip IN :cips ORDER BY p.cip")
    List<Pharmacy> fetchPharmacyInformationsForCips(@Param("cips") List<String> cips);

    default List<Pharmacy> findPharmaciesByCips(List<String> cips, java.util.UUID accountId) {
        if (cips == null || cips.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        // Fetch pharmacies first
        List<Pharmacy> pharmacies = findPharmaciesByCipsBase(cips);
        if (!pharmacies.isEmpty()) {
            // Then fetch informations
            pharmacies = fetchPharmacyInformationsForCips(cips);
        }
        return pharmacies;
    }

    @Query(value = "SELECT DISTINCT p.cip FROM pharmacy p " +
           "LEFT JOIN pharmacy_informations pi ON p.cip = pi.cip AND CAST(pi.id_account AS VARCHAR) = :accountId " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:city IS NULL OR " +
           "    (LOWER(COALESCE(pi.city, p.city)) LIKE LOWER(CONCAT('%', :city, '%')) OR " +
           "     (:cityAlias IS NOT NULL AND LOWER(COALESCE(pi.city, p.city)) LIKE LOWER(CONCAT('%', :cityAlias, '%'))))) " +
           "AND (:postalCode IS NULL OR LOWER(COALESCE(pi.postal_code, p.postal_code)) LIKE LOWER(CONCAT('%', :postalCode, '%'))) " +
           "AND (:cip IS NULL OR LOWER(p.cip) LIKE LOWER(CONCAT('%', :cip, '%'))) " +
           "AND (:address IS NULL OR " +
           "    LOWER(COALESCE(pi.address1, p.address1)) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(COALESCE(pi.address2, p.address2)) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(COALESCE(pi.address3, p.address3)) LIKE LOWER(CONCAT('%', :address, '%'))) " +
           "AND (:isLocationValid IS NULL OR " +
           "    (:isLocationValid = true AND COALESCE(pi.latitude, p.latitude) IS NOT NULL AND COALESCE(pi.latitude, p.latitude) != 0 AND COALESCE(pi.longitude, p.longitude) IS NOT NULL AND COALESCE(pi.longitude, p.longitude) != 0) OR " +
           "    (:isLocationValid = false AND (COALESCE(pi.latitude, p.latitude) IS NULL OR COALESCE(pi.latitude, p.latitude) = 0 OR COALESCE(pi.longitude, p.longitude) IS NULL OR COALESCE(pi.longitude, p.longitude) = 0))) " +
           "AND (:zoneId IS NULL OR " +
           "    (:zoneId = 'none' AND pi.id_zone IS NULL) OR " +
           "    (:zoneId IS NOT NULL AND :zoneId != 'none' AND CAST(pi.id_zone AS VARCHAR) = :zoneId)) " +
           "AND (:hasOrdered IS NULL OR " +
           "    (:hasOrdered = true AND (pi.never_ordered IS NULL OR pi.never_ordered = false)) OR " +
           "    (:hasOrdered = false AND pi.never_ordered = true))" +
           " ORDER BY p.cip LIMIT :maxResults",
           nativeQuery = true)
    List<String> searchPharmaciesCipsByAccount(
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