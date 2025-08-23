package bzh.stack.apimovix.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Sender;

@Repository
public interface SenderRepository extends JpaRepository<Sender, String> {
    Optional<Sender> findFirstByAccount(Account account);
} 