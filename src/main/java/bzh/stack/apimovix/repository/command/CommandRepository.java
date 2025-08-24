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

    @Query("SELECT c FROM Command c WHERE c.pharmacy.cip = :cip AND (c.lastHistoryStatus IS NULL OR c.lastHistoryStatus.status.id = 1) AND DATE(c.expDate) = DATE(:date) ORDER BY c.expDate DESC LIMIT 1")
    public Command findPharmacyCommandByDate(@Param("cip") String cip, @Param("date") LocalDateTime date);


    @Query("SELECT DISTINCT c FROM Command c LEFT JOIN FETCH c.tour t WHERE c.id IN :ids AND c.sender.account = :account")
    public List<Command> findCommandsByIds(@Param("account") Account account, @Param("ids") List<UUID> ids);

    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandExpeditionDTO(" +
           "c.id, " +
           "c.closeDate, " +
           "c.tourOrder, " +
           "c.expDate, " +
           "c.comment, " +
           "c.newPharmacy, " +
           "c.latitude, " +
           "c.longitude, " +
           "new bzh.stack.apimovix.dto.command.CommandTourDTO(c.tour.id, c.tour.name, c.tour.color), " +
           "(SELECT COUNT(p) FROM PackageEntity p WHERE p.command = c), " +
           "(SELECT COALESCE(SUM(p.weight), 0.0) FROM PackageEntity p WHERE p.command = c), " +
           "new bzh.stack.apimovix.dto.pharmacy.PharmacyDTO(c.pharmacy.cip, c.pharmacy.name, c.pharmacy.address1, c.pharmacy.city, c.pharmacy.postalCode, c.pharmacy.latitude, c.pharmacy.longitude), " +
           "c.pharmacy.commentaire, " +
           "c.lastHistoryStatus.status) " +
           "FROM Command c " +
           "LEFT JOIN c.tour t " +
           "WHERE (t.account = :account OR t IS NULL) " +
           "AND c.expDate >= :startDate AND c.expDate < :endDate " +
           "ORDER BY COALESCE(c.tour.id, ''), c.expDate DESC")
    public List<CommandExpeditionDTO> findExpeditionCommands(@Param("account") Account account, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Command c WHERE c.id = :id AND c.sender.account = :account")
    public Command findCommandById(Account account, UUID id);

    @Query("SELECT c FROM Command c WHERE c.pharmacy.cip = :cip ORDER BY c.expDate DESC LIMIT 5")
    public List<Command> findLast5CommandsByPharmacyCip(@Param("cip") String cip);

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
           "c.pharmacy.latitude, \n" +
           "c.pharmacy.longitude, \n" +
           "c.pharmacy.name, \n" +
           "c.pharmacy.city, \n" +
           "c.pharmacy.postalCode, \n" +
           "c.pharmacy.address1, \n" +
           "c.pharmacy.address2, \n" +
           "c.pharmacy.address3 \n" +
           ") FROM Command c \n" +
           "WHERE (c.sender.account = :account) \n" +
           "AND (:name IS NULL OR LOWER(c.pharmacy.name) LIKE %:name%) \n" +
           "AND (:city IS NULL OR LOWER(c.pharmacy.city) LIKE %:city%) \n" +
           "AND (:commandId IS NULL OR STR(c.id) LIKE %:commandId%) \n" +
           "AND (:pharmacyCip IS NULL OR c.pharmacy.cip LIKE %:pharmacyCip%) \n" +
           "AND (:pharmacyPostalCode IS NULL OR c.pharmacy.postalCode LIKE %:pharmacyPostalCode%) \n" +
           "AND (:address IS NULL OR LOWER(c.pharmacy.address1) LIKE %:address% OR LOWER(c.pharmacy.address2) LIKE %:address% OR LOWER(c.pharmacy.address3) LIKE %:address%) \n" +
           "ORDER BY c.expDate DESC \n" +
           "LIMIT 100")
    public List<CommandSearchResponseDTO> searchCommandsWithoutDates(
        @Param("account") Account account, 
        @Param("name") String name, 
        @Param("city") String city, 
        @Param("pharmacyCip") String pharmacyCip, 
        @Param("pharmacyPostalCode") String pharmacyPostalCode, 
        @Param("address") String address, 
        @Param("commandId") String commandId);

    @Query("SELECT new bzh.stack.apimovix.dto.command.CommandSearchResponseDTO(\n" +
           "c.id, \n" +
           "c.closeDate, \n" +
           "c.expDate, \n" +
           "c.comment, \n" +
           "c.newPharmacy, \n" +
           "c.pharmacy.latitude, \n" +
           "c.pharmacy.longitude, \n" +
           "c.pharmacy.name, \n" +
           "c.pharmacy.city, \n" +
           "c.pharmacy.postalCode, \n" +
           "c.pharmacy.address1, \n" +
           "c.pharmacy.address2, \n" +
           "c.pharmacy.address3 \n" +
           ") FROM Command c \n" +
           "WHERE (c.sender.account = :account) \n" +
           "AND (:name IS NULL OR LOWER(c.pharmacy.name) LIKE %:name%) \n" +
           "AND (:city IS NULL OR LOWER(c.pharmacy.city) LIKE %:city%) \n" +
           "AND (:commandId IS NULL OR STR(c.id) LIKE %:commandId%) \n" +
           "AND (:pharmacyCip IS NULL OR c.pharmacy.cip LIKE %:pharmacyCip%) \n" +
           "AND (:pharmacyPostalCode IS NULL OR c.pharmacy.postalCode LIKE %:pharmacyPostalCode%) \n" +
           "AND (:address IS NULL OR LOWER(c.pharmacy.address1) LIKE %:address% OR LOWER(c.pharmacy.address2) LIKE %:address% OR LOWER(c.pharmacy.address3) LIKE %:address%) \n" +
           "AND c.expDate >= :startDate AND c.expDate <= :endDate \n" +
           "ORDER BY c.expDate DESC \n" +
           "LIMIT 100")
    public List<CommandSearchResponseDTO> searchCommandsWithDates(
        @Param("account") Account account, 
        @Param("name") String name, 
        @Param("city") String city, 
        @Param("pharmacyCip") String pharmacyCip, 
        @Param("pharmacyPostalCode") String pharmacyPostalCode, 
        @Param("address") String address, 
        @Param("commandId") String commandId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    
} 