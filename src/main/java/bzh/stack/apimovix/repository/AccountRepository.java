package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.profils WHERE a.autoCreateTour = true AND a.isActive = true")
    List<Account> findAllWithAutoCreateTourEnabled();
} 