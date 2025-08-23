package bzh.stack.apimovix.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.model.Todo;
import bzh.stack.apimovix.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Tag(name = "Todos", description = "API for managing todo items")
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    @Operation(summary = "Get all todos", description = "Retrieves a list of all todo items sorted by creation date", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all todos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class, type = "array")))
    })
    public ResponseEntity<List<Todo>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get todo by ID", description = "Retrieves a specific todo item by its unique identifier", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the todo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found", content = @Content)
    })
    public ResponseEntity<Todo> getTodoById(
            @Parameter(description = "ID of the todo to retrieve", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodoById(id));
    }

    @PostMapping
    @Operation(summary = "Create new todo", description = "Creates a new todo item with the provided information", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully created the todo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class)))
    })
    public ResponseEntity<Todo> createTodo(
            @Parameter(description = "Todo object to create", required = true) @RequestBody Todo todo) {
        return ResponseEntity.ok(todoService.createTodo(todo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update todo", description = "Updates an existing todo item with new information", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the todo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found", content = @Content)
    })
    public ResponseEntity<Todo> updateTodo(
            @Parameter(description = "ID of the todo to update", required = true) @PathVariable Long id,
            @Parameter(description = "Updated todo information", required = true) @RequestBody Todo todo) {
        return ResponseEntity.ok(todoService.updateTodo(id, todo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete todo", description = "Deletes an existing todo item", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the todo"),
            @ApiResponse(responseCode = "404", description = "Todo not found", content = @Content)
    })
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "ID of the todo to delete", required = true) @PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed todos", description = "Retrieves a list of all completed todo items sorted by creation date", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved completed todos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class, type = "array")))
    })
    public ResponseEntity<List<Todo>> getCompletedTodos() {
        return ResponseEntity.ok(todoService.getCompletedTodos());
    }

    @GetMapping("/uncompleted")
    @Operation(summary = "Get uncompleted todos", description = "Retrieves a list of all uncompleted todo items sorted by creation date", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved uncompleted todos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Todo.class, type = "array")))
    })
    public ResponseEntity<List<Todo>> getUncompletedTodos() {
        return ResponseEntity.ok(todoService.getUncompletedTodos());
    }
}