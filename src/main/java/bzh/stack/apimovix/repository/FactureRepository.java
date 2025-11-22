package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Facture;

@Repository
public interface FactureRepository extends JpaRepository<Facture, UUID> {

    List<Facture> findByAccountIdOrderByDateFactureDesc(UUID accountId);
}
