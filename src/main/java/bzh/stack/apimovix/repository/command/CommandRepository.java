package bzh.stack.apimovix.repository.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.dto.command.CommandExpeditionDTO;
import bzh.stack.apimovix.dto.command.CommandSearchResponseDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.History.HistoryCommandStatus;

@Repository
public interface CommandRepository extends JpaRepository<Command, UUID> {

    @Query("SELECT c FROM Command c " +
           "LEFT JOIN FETCH c.pharmacy p " +
           "WHERE c.pharmacy.cip = :cip AND c.sender.account = :account " +
           "AND (c.lastHistoryStatus IS NULL OR c.lastHistoryStatus.status.id = 1) " +
           "AND DATE(c.expDate) = DATE(:date) " +
           "ORDER BY c.expDate DESC LIMIT 1")
    public Command findPharmacyCommandByDate(@Param("account") Account account, @Param("cip") String cip, @Param("date") LocalDateTime date);


    @Query("SELECT DISTINCT c FROM Command c " +
           "LEFT JOIN FETCH c.tour t " +
           "LEFT JOIN FETCH c.pharmacy p " +
           "LEFT JOIN FETCH p.pharmacyInformationsList " +
           "WHERE c.id IN :ids AND c.sender.account = :account")
    public List<Command> findCommandsByIds(@Param("account") Account account, @Param("ids") List<UUID> ids);

    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandExpeditionDTO(" +
           "c.id, c.closeDate, c.tourOrder, c.expDate, c.comment, c.newPharmacy, " +
           "c.latitude, c.longitude, " +
           "t.id, t.name, t.color, " +
           "(SELECT COUNT(pkg) FROM PackageEntity pkg WHERE pkg.command = c), " +
           "(SELECT COALESCE(SUM(pkg.weight), 0.0) FROM PackageEntity pkg WHERE pkg.command = c), " +
           "c.pharmacy.cip, " +
           "COALESCE(pi.name, c.pharmacy.name), " +
           "COALESCE(pi.address1, c.pharmacy.address1), " +
           "COALESCE(pi.city, c.pharmacy.city), " +
           "COALESCE(pi.postalCode, c.pharmacy.postalCode), " +
           "COALESCE(pi.latitude, c.pharmacy.latitude), " +
           "COALESCE(pi.longitude, c.pharmacy.longitude), " +
           "pi.commentaire, " +
           "c.lastHistoryStatus.status) " +
           "FROM Command c " +
           "LEFT JOIN c.tour t " +
           "LEFT JOIN c.pharmacy.pharmacyInformationsList pi WITH pi.account = :account " +
           "WHERE c.sender.account = :account " +
           "AND c.expDate >= :startDate AND c.expDate < :endDate " +
           "ORDER BY COALESCE(t.id, ''), c.expDate DESC")
    public List<CommandExpeditionDTO> findExpeditionCommandsOptimized(@Param("account") Account account, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandExpeditionDTO(" +
           "c.id, " +
           "c.closeDate, " +
           "c.tourOrder, " +
           "c.expDate, " +
           "c.comment, " +
           "c.newPharmacy, " +
           "c.latitude, " +
           "c.longitude, " +
           "c.tour, " +
           "(SELECT COUNT(p) FROM PackageEntity p WHERE p.command = c), " +
           "(SELECT COALESCE(SUM(p.weight), 0.0) FROM PackageEntity p WHERE p.command = c), " +
           "new bzh.stack.apimovix.dto.pharmacy.PharmacyDTO(c.pharmacy.cip, c.pharmacy.name, c.pharmacy.address1, c.pharmacy.city, c.pharmacy.postalCode, c.pharmacy.latitude, c.pharmacy.longitude), " +
           "c.lastHistoryStatus.status) " +
           "FROM Command c " +
           "LEFT JOIN c.tour t " +
           "WHERE c.sender.account = :account " +
           "AND c.expDate >= :startDate AND c.expDate < :endDate " +
           "ORDER BY COALESCE(c.tour.id, ''), c.expDate DESC")
    public List<CommandExpeditionDTO> findExpeditionCommands(@Param("account") Account account, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Command c " +
           "LEFT JOIN FETCH c.pharmacy p " +
           "LEFT JOIN FETCH p.pharmacyInformationsList " +
           "WHERE c.id = :id AND c.sender.account = :account")
    public Command findCommandById(Account account, UUID id);

    @Query("SELECT c FROM Command c WHERE c.pharmacy.cip = :cip AND c.sender.account = :account AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL AND c.latitude != 0 AND c.longitude != 0 ORDER BY c.expDate DESC LIMIT 5")
    public List<Command> findLast5CommandsByPharmacyCip(@Param("account") Account account, @Param("cip") String cip);

    @Query("SELECT c FROM Command c WHERE c.pharmacy.cip = :cip")
    public List<Command> findAllCommandsByPharmacyCip(@Param("cip") String cip);

    @Query("SELECT COALESCE(MAX(c.tourOrder), 0) FROM Command c WHERE c.tour.id = :tourId")
    public Integer findMaxTourOrderByTourId(@Param("tourId") String tourId);

    @Query("SELECT hs FROM HistoryCommandStatus hs WHERE hs.command.sender.account = :account AND hs.command.id = :id ORDER BY hs.createdAt DESC")
    public List<HistoryCommandStatus> findCommandHistory(Account account, UUID id);

    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandSearchResponseDTO(\n" +
           "c.id, \n" +
           "c.closeDate, \n" +
           "c.expDate, \n" +
           "c.comment, \n" +
           "c.newPharmacy, \n" +
           "COALESCE(pi.latitude, c.pharmacy.latitude), \n" +
           "COALESCE(pi.longitude, c.pharmacy.longitude), \n" +
           "COALESCE(pi.name, c.pharmacy.name), \n" +
           "COALESCE(pi.city, c.pharmacy.city), \n" +
           "COALESCE(pi.postalCode, c.pharmacy.postalCode), \n" +
           "COALESCE(pi.address1, c.pharmacy.address1), \n" +
           "COALESCE(pi.address2, c.pharmacy.address2), \n" +
           "COALESCE(pi.address3, c.pharmacy.address3) \n" +
           ") FROM Command c \n" +
           "LEFT JOIN c.pharmacy.pharmacyInformationsList pi WITH pi.account = :account \n" +
           "WHERE (c.sender.account = :account) \n" +
           "AND LENGTH(c.pharmacy.cip) > 5 \n" +
           "AND (:name IS NULL OR LOWER(COALESCE(pi.name, c.pharmacy.name)) LIKE %:name%) \n" +
           "AND (:city IS NULL OR LOWER(COALESCE(pi.city, c.pharmacy.city)) LIKE %:city%) \n" +
           "AND (:commandId IS NULL OR STR(c.id) LIKE %:commandId%) \n" +
           "AND (:pharmacyCip IS NULL OR c.pharmacy.cip LIKE %:pharmacyCip%) \n" +
           "AND (:pharmacyPostalCode IS NULL OR COALESCE(pi.postalCode, c.pharmacy.postalCode) LIKE %:pharmacyPostalCode%) \n" +
           "AND (:address IS NULL OR LOWER(COALESCE(pi.address1, c.pharmacy.address1)) LIKE %:address% OR LOWER(COALESCE(pi.address2, c.pharmacy.address2)) LIKE %:address% OR LOWER(COALESCE(pi.address3, c.pharmacy.address3)) LIKE %:address%) \n" +
           "ORDER BY c.expDate DESC LIMIT :limit OFFSET :offset")
    public List<CommandSearchResponseDTO> searchCommandsWithoutDates(
        @Param("account") Account account,
        @Param("name") String name,
        @Param("city") String city,
        @Param("pharmacyCip") String pharmacyCip,
        @Param("pharmacyPostalCode") String pharmacyPostalCode,
        @Param("address") String address,
        @Param("commandId") String commandId,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset);

    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandSearchResponseDTO(\n" +
           "c.id, \n" +
           "c.closeDate, \n" +
           "c.expDate, \n" +
           "c.comment, \n" +
           "c.newPharmacy, \n" +
           "COALESCE(pi.latitude, c.pharmacy.latitude), \n" +
           "COALESCE(pi.longitude, c.pharmacy.longitude), \n" +
           "COALESCE(pi.name, c.pharmacy.name), \n" +
           "COALESCE(pi.city, c.pharmacy.city), \n" +
           "COALESCE(pi.postalCode, c.pharmacy.postalCode), \n" +
           "COALESCE(pi.address1, c.pharmacy.address1), \n" +
           "COALESCE(pi.address2, c.pharmacy.address2), \n" +
           "COALESCE(pi.address3, c.pharmacy.address3) \n" +
           ") FROM Command c \n" +
           "LEFT JOIN c.pharmacy.pharmacyInformationsList pi WITH pi.account = :account \n" +
           "WHERE (c.sender.account = :account) \n" +
           "AND LENGTH(c.pharmacy.cip) > 5 \n" +
           "AND (:name IS NULL OR LOWER(COALESCE(pi.name, c.pharmacy.name)) LIKE %:name%) \n" +
           "AND (:city IS NULL OR LOWER(COALESCE(pi.city, c.pharmacy.city)) LIKE %:city%) \n" +
           "AND (:commandId IS NULL OR STR(c.id) LIKE %:commandId%) \n" +
           "AND (:pharmacyCip IS NULL OR c.pharmacy.cip LIKE %:pharmacyCip%) \n" +
           "AND (:pharmacyPostalCode IS NULL OR COALESCE(pi.postalCode, c.pharmacy.postalCode) LIKE %:pharmacyPostalCode%) \n" +
           "AND (:address IS NULL OR LOWER(COALESCE(pi.address1, c.pharmacy.address1)) LIKE %:address% OR LOWER(COALESCE(pi.address2, c.pharmacy.address2)) LIKE %:address% OR LOWER(COALESCE(pi.address3, c.pharmacy.address3)) LIKE %:address%) \n" +
           "AND c.expDate >= :startDate AND c.expDate <= :endDate \n" +
           "ORDER BY c.expDate DESC LIMIT :limit OFFSET :offset")
    public List<CommandSearchResponseDTO> searchCommandsWithDates(
        @Param("account") Account account,
        @Param("name") String name,
        @Param("city") String city,
        @Param("pharmacyCip") String pharmacyCip,
        @Param("pharmacyPostalCode") String pharmacyPostalCode,
        @Param("address") String address,
        @Param("commandId") String commandId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset);

    // Recherche globale sans dates (multi-mots)
    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandSearchResponseDTO(\n" +
           "c.id, \n" +
           "c.closeDate, \n" +
           "c.expDate, \n" +
           "c.comment, \n" +
           "c.newPharmacy, \n" +
           "COALESCE(pi.latitude, c.pharmacy.latitude), \n" +
           "COALESCE(pi.longitude, c.pharmacy.longitude), \n" +
           "COALESCE(pi.name, c.pharmacy.name), \n" +
           "COALESCE(pi.city, c.pharmacy.city), \n" +
           "COALESCE(pi.postalCode, c.pharmacy.postalCode), \n" +
           "COALESCE(pi.address1, c.pharmacy.address1), \n" +
           "COALESCE(pi.address2, c.pharmacy.address2), \n" +
           "COALESCE(pi.address3, c.pharmacy.address3) \n" +
           ") FROM Command c \n" +
           "LEFT JOIN c.pharmacy.pharmacyInformationsList pi WITH pi.account = :account \n" +
           "WHERE (c.sender.account = :account) \n" +
           "AND LENGTH(c.pharmacy.cip) > 5 \n" +
           "AND (:q1 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q1%) \n" +
           "AND (:q2 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q2%) \n" +
           "AND (:q3 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q3%) \n" +
           "AND (:q4 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q4%) \n" +
           "AND (:q5 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q5%) \n" +
           "ORDER BY c.expDate DESC LIMIT :limit OFFSET :offset")
    public List<CommandSearchResponseDTO> searchCommandsGlobalWithoutDates(
        @Param("account") Account account,
        @Param("q1") String q1,
        @Param("q2") String q2,
        @Param("q3") String q3,
        @Param("q4") String q4,
        @Param("q5") String q5,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset);

    // Recherche globale avec dates (multi-mots)
    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandSearchResponseDTO(\n" +
           "c.id, \n" +
           "c.closeDate, \n" +
           "c.expDate, \n" +
           "c.comment, \n" +
           "c.newPharmacy, \n" +
           "COALESCE(pi.latitude, c.pharmacy.latitude), \n" +
           "COALESCE(pi.longitude, c.pharmacy.longitude), \n" +
           "COALESCE(pi.name, c.pharmacy.name), \n" +
           "COALESCE(pi.city, c.pharmacy.city), \n" +
           "COALESCE(pi.postalCode, c.pharmacy.postalCode), \n" +
           "COALESCE(pi.address1, c.pharmacy.address1), \n" +
           "COALESCE(pi.address2, c.pharmacy.address2), \n" +
           "COALESCE(pi.address3, c.pharmacy.address3) \n" +
           ") FROM Command c \n" +
           "LEFT JOIN c.pharmacy.pharmacyInformationsList pi WITH pi.account = :account \n" +
           "WHERE (c.sender.account = :account) \n" +
           "AND LENGTH(c.pharmacy.cip) > 5 \n" +
           "AND (:q1 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q1%) \n" +
           "AND (:q2 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q2%) \n" +
           "AND (:q3 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q3%) \n" +
           "AND (:q4 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q4%) \n" +
           "AND (:q5 IS NULL OR LOWER(CONCAT(COALESCE(pi.name, c.pharmacy.name), ' ', c.pharmacy.cip, ' ', COALESCE(pi.city, c.pharmacy.city), ' ', COALESCE(pi.postalCode, c.pharmacy.postalCode), ' ', COALESCE(pi.address1, c.pharmacy.address1), ' ', COALESCE(pi.address2, c.pharmacy.address2, ''), ' ', COALESCE(pi.address3, c.pharmacy.address3, ''), ' ', STR(c.id))) LIKE %:q5%) \n" +
           "AND c.expDate >= :startDate AND c.expDate <= :endDate \n" +
           "ORDER BY c.expDate DESC LIMIT :limit OFFSET :offset")
    public List<CommandSearchResponseDTO> searchCommandsGlobalWithDates(
        @Param("account") Account account,
        @Param("q1") String q1,
        @Param("q2") String q2,
        @Param("q3") String q3,
        @Param("q4") String q4,
        @Param("q5") String q5,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") Integer limit,
        @Param("offset") Integer offset);
} 