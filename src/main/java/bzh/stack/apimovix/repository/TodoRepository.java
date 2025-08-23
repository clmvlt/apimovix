package bzh.stack.apimovix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByOrderByCreatedAtDesc();
    List<Todo> findByCompletedTrueOrderByCreatedAtDesc();
    List<Todo> findByCompletedFalseOrderByCreatedAtDesc();
} 