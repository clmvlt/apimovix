package bzh.stack.apimovix.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Picture.AccountLogo;

@Repository
public interface AccountLogoRepository extends JpaRepository<AccountLogo, UUID> {
    Optional<AccountLogo> findByAccount_Id(UUID accountId);
}
