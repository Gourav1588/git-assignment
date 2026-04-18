package com.training.todoapp.controller;

import com.training.todoapp.dto.TodoDTO;
import com.training.todoapp.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// I use @RestController to automatically serialize all our responses into JSON format.
@RestController
@RequestMapping("/todos")
public class TodoController {

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<TodoDTO> createTodo(@Valid @RequestBody TodoDTO dto) {
        // I log the incoming HTTP verb and path at the edge before it hits business logic.
        log.info("POST /todos — title='{}'", dto.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(dto));
    }

    @GetMapping
    public ResponseEntity<List<TodoDTO>> getAllTodos() {
        log.info("GET /todos");
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable Long id) {
        log.info("GET /todos/{}", id);
        return ResponseEntity.ok(todoService.getTodoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(@PathVariable Long id,
                                              @Valid @RequestBody TodoDTO dto) {
        log.info("PUT /todos/{}", id);
        return ResponseEntity.ok(todoService.updateTodo(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TodoDTO> patchTodo(@PathVariable Long id,
                                             @RequestBody TodoDTO dto) {
        log.info("PATCH /todos/{}", id);
        return ResponseEntity.ok(todoService.updateTodo(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        log.info("DELETE /todos/{}", id);
        todoService.deleteTodo(id);
        // I'm returning 204 No Content for deletion.
        return ResponseEntity.noContent().build();
    }
}