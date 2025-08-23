package bzh.stack.apimovix.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.Todo;
import bzh.stack.apimovix.repository.TodoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Todo> getAllTodos() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }

    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo non trouvé avec l'id: " + id));
    }

    public Todo createTodo(Todo todo) {
        String currentTime = LocalDateTime.now().format(formatter);
        todo.setCreatedAt(currentTime);
        todo.setUpdatedAt(currentTime);
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long id, Todo todoDetails) {
        Todo todo = getTodoById(id);
        
        todo.setTitle(todoDetails.getTitle());
        todo.setDescription(todoDetails.getDescription());
        todo.setCompleted(todoDetails.isCompleted());
        todo.setUpdatedAt(LocalDateTime.now().format(formatter));
        
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id) {
        Todo todo = getTodoById(id);
        todoRepository.delete(todo);
    }

    public List<Todo> getCompletedTodos() {
        return todoRepository.findByCompletedTrueOrderByCreatedAtDesc();
    }

    public List<Todo> getUncompletedTodos() {
        return todoRepository.findByCompletedFalseOrderByCreatedAtDesc();
    }
} 