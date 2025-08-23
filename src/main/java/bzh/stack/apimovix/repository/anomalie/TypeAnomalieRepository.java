package bzh.stack.apimovix.repository.anomalie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.StatusType.TypeAnomalie;

@Repository
public interface TypeAnomalieRepository extends JpaRepository<TypeAnomalie, String> {
    @Query("SELECT ta FROM TypeAnomalie ta WHERE ta.code = :code")
    public Optional<TypeAnomalie> findTypeAnomalie(@Param("code")  String code);
} 