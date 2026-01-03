package bzh.stack.apimovix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.TodoCategory;

@Repository
public interface TodoCategoryRepository extends JpaRepository<TodoCategory, Long> {
    List<TodoCategory> findAllByOrderByCreatedAtDesc();
}
