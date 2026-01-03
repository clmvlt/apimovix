package bzh.stack.apimovix.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.TodoCategory;
import bzh.stack.apimovix.repository.TodoCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoCategoryService {

    private final TodoCategoryRepository todoCategoryRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<TodoCategory> getAllCategories() {
        return todoCategoryRepository.findAllByOrderByCreatedAtDesc();
    }

    public TodoCategory getCategoryById(Long id) {
        return todoCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id: " + id));
    }

    public TodoCategory createCategory(TodoCategory category) {
        String currentTime = LocalDateTime.now().format(formatter);
        category.setCreatedAt(currentTime);
        return todoCategoryRepository.save(category);
    }

    public TodoCategory updateCategory(Long id, TodoCategory categoryDetails) {
        TodoCategory category = getCategoryById(id);

        category.setName(categoryDetails.getName());
        category.setColor(categoryDetails.getColor());

        return todoCategoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        TodoCategory category = getCategoryById(id);
        todoCategoryRepository.delete(category);
    }
}
