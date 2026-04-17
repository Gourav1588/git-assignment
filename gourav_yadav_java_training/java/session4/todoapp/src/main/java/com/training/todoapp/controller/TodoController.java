package com.training.todoapp.controller;

import com.training.todoapp.dto.TodoDTO;
import com.training.todoapp.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos") // Base URL for all Todo APIs
public class TodoController {

    private final TodoService todoService;

    // Inject service layer (business logic)
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // Create a new Todo.
    @PostMapping
    public ResponseEntity<TodoDTO> createTodo(@Valid @RequestBody TodoDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(todoService.createTodo(dto));
    }

    // Fetch all Todos
    @GetMapping
    public ResponseEntity<List<TodoDTO>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    // Fetch a single Todo by ID
    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodoById(id));
    }

    // Update an existing Todo
    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(@PathVariable Long id,
                                              @Valid @RequestBody TodoDTO dto) {
        return ResponseEntity.ok(todoService.updateTodo(id, dto));
    }

    // Partial update Todo
    @PatchMapping("/{id}")
    public ResponseEntity<TodoDTO> patchTodo(
            @PathVariable Long id,
            @RequestBody TodoDTO dto) {

        return ResponseEntity.ok(todoService.updateTodo(id, dto));
    }

    // Delete a Todo by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.deleteTodo(id));
    }
}