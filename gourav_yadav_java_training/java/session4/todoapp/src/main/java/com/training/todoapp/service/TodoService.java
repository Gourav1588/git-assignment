package com.training.todoapp.service;

import com.training.todoapp.dto.TodoDTO;
import com.training.todoapp.entity.Todo;
import com.training.todoapp.exception.TodoNotFoundException;
import com.training.todoapp.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    // Constructor injection for required dependencies
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Creates a new Todo task with default status and timestamp.
     */
    public TodoDTO createTodo(TodoDTO dto) {
        Todo todo = new Todo(dto.getTitle(), dto.getDescription(), dto.getStatus());
        return convertToDTO(todoRepository.save(todo));
    }

    /**
     * Retrieves all Todo tasks from the database.
     */
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single Todo by its ID. Throws an exception if not found.
     */
    public TodoDTO getTodoById(Long id) {
        return convertToDTO(getTodoEntity(id));
    }

    /**
     * Updates an existing Todo. Enforces status transition rules.
     */
    public TodoDTO updateTodo(Long id, TodoDTO dto) {
        Todo todo = getTodoEntity(id);

        // Prevent invalid same-state transitions (e.g., PENDING -> PENDING)
        if (dto.getStatus() != null) {
            if (todo.getStatus().equals(dto.getStatus())) {
                throw new IllegalArgumentException("Invalid transition. Status is already " + todo.getStatus());
            }
            todo.setStatus(dto.getStatus());
        }

        // Apply partial updates if fields are provided
        if (dto.getTitle() != null) todo.setTitle(dto.getTitle());
        if (dto.getDescription() != null) todo.setDescription(dto.getDescription());

        return convertToDTO(todoRepository.save(todo));
    }

    /**
     * Deletes a Todo by its ID.
     */
    public String deleteTodo(Long id) {
        todoRepository.delete(getTodoEntity(id));
        return "Todo with id " + id + " deleted successfully";
    }

    // Helper method to fetch entity and handle 404s
    private Todo getTodoEntity(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
    }

    // Manual mapping from Entity to DTO to prevent exposing database models
    private TodoDTO convertToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setStatus(todo.getStatus());
        dto.setCreatedAt(todo.getCreatedAt());
        return dto;
    }
}