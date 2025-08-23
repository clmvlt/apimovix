package bzh.stack.apimovix.repository.pharmacy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Pharmacy;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, String> {
    
    @Query("SELECT DISTINCT p FROM Pharmacy p LEFT JOIN FETCH p.pictures")
    List<Pharmacy> findPharmacies();
    
    @Query("SELECT p FROM Pharmacy p LEFT JOIN FETCH p.pictures WHERE p.cip = :cip")
    Pharmacy findPharmacy(@Param("cip") String cip);

    @Query(value = "SELECT DISTINCT p.* FROM pharmacy p " +
           "LEFT JOIN pharmacy_picture pp ON p.cip = pp.cip " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:postalCode IS NULL OR LOWER(p.postal_code) LIKE LOWER(CONCAT('%', :postalCode, '%'))) " +
           "AND (:cip IS NULL OR LOWER(p.cip) LIKE LOWER(CONCAT('%', :cip, '%'))) " +
           "AND (:address IS NULL OR " +
           "    LOWER(p.address1) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address2) LIKE LOWER(CONCAT('%', :address, '%')) OR " +
           "    LOWER(p.address3) LIKE LOWER(CONCAT('%', :address, '%')))" +
           " ORDER BY p.cip LIMIT 100", 
           nativeQuery = true)
    List<Pharmacy> searchPharmacies(
        @Param("name") String name,
        @Param("city") String city,
        @Param("postalCode") String postalCode,
        @Param("cip") String cip,
        @Param("address") String address
    );
} 