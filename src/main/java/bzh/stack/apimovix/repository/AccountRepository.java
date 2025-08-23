package bzh.stack.apimovix.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
} 