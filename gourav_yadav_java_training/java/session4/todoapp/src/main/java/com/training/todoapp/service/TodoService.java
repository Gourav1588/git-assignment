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

    // Service layer handles business logic and DTO ↔ Entity conversion
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // CREATE
    public TodoDTO createTodo(TodoDTO dto) {
        Todo todo = new Todo(
                dto.getTitle(),
                dto.getDescription(),
                dto.getStatus()
        );
        return convertToDTO(todoRepository.save(todo));
    }


    // GET ALL
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public TodoDTO getTodoById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
        return convertToDTO(todo);
    }

    // UPDATE
    public TodoDTO updateTodo(Long id, TodoDTO dto) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));

        // Allow both PENDING→COMPLETED and COMPLETED→PENDING
        if (dto.getStatus() != null && !dto.getStatus().equals(todo.getStatus())) {
            todo.setStatus(dto.getStatus());
        }

        if (dto.getTitle() != null) todo.setTitle(dto.getTitle());
        if (dto.getDescription() != null) todo.setDescription(dto.getDescription());

        Todo updated = todoRepository.save(todo);
        return convertToDTO(updated);
    }



    // DELETE
    public String deleteTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
        todoRepository.delete(todo);
        return "Todo with id " + id + " deleted successfully";
    }

    // Convert Entity → DTO to control response data
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
