package com.training.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.training.todoapp.dto.TodoDTO;
import com.training.todoapp.entity.Todo.Status;
import com.training.todoapp.exception.GlobalExceptionHandler;
import com.training.todoapp.exception.TodoNotFoundException;
import com.training.todoapp.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    MockMvc mockMvc;

    @Mock TodoService todoService;
    @InjectMocks TodoController todoController;

    // I register the JavaTimeModule so Jackson can serialize LocalDateTime fields properly.
    final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    TodoDTO sample;

    @BeforeEach
    void setUp() {
        // I use standaloneSetup to bypass loading the full Spring Boot context, making tests lightning fast.
        mockMvc = MockMvcBuilders
                .standaloneSetup(todoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sample = new TodoDTO();
        sample.setId(1L);
        sample.setTitle("Buy groceries");
        sample.setDescription("Milk and eggs");
        sample.setStatus(Status.PENDING);
        sample.setCreatedAt(LocalDateTime.now());
    }

    // POST /todos

    @Test
    void createTodo_returns201() throws Exception {
        when(todoService.createTodo(any())).thenReturn(sample);

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Buy groceries"));
    }

    @Test
    void createTodo_missingTitle_returns400() throws Exception {
        TodoDTO bad = new TodoDTO();

        // I intentionally send bad data to verify the @Valid annotations intercept it correctly.
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void createTodo_shortTitle_returns400() throws Exception {
        TodoDTO bad = new TodoDTO();
        bad.setTitle("ab");

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void createTodo_blankTitle_returns400() throws Exception {
        TodoDTO bad = new TodoDTO();
        bad.setTitle("   "); // whitespace only

        // I verify that blank titles are also rejected after switching to @NotBlank.
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    // GET /todos

    @Test
    void getAllTodos_returns200WithList() throws Exception {
        when(todoService.getAllTodos()).thenReturn(List.of(sample));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy groceries"));
    }

    @Test
    void getAllTodos_whenEmpty_returnsEmptyArray() throws Exception {
        when(todoService.getAllTodos()).thenReturn(List.of());

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /todos/{id}

    @Test
    void getTodoById_whenFound_returns200() throws Exception {
        when(todoService.getTodoById(1L)).thenReturn(sample);

        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getTodoById_whenNotFound_returns404() throws Exception {
        when(todoService.getTodoById(99L))
                .thenThrow(new TodoNotFoundException("Todo not found with id: 99"));

        mockMvc.perform(get("/todos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Todo not found with id: 99"));
    }

    // PUT /todos/{id}

    @Test
    void updateTodo_returns200() throws Exception {
        sample.setStatus(Status.COMPLETED);
        when(todoService.updateTodo(eq(1L), any())).thenReturn(sample);

        mockMvc.perform(put("/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateTodo_sameStatus_returns400() throws Exception {
        when(todoService.updateTodo(eq(1L), any()))
                .thenThrow(new IllegalArgumentException("Status is already PENDING"));

        mockMvc.perform(put("/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sample)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Status is already PENDING"));
    }

    // PATCH /todos/{id}

    @Test
    void patchTodo_returns200() throws Exception {
        when(todoService.updateTodo(eq(1L), any())).thenReturn(sample);

        TodoDTO patch = new TodoDTO();
        patch.setTitle("Updated title");

        mockMvc.perform(patch("/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk());
    }

    // DELETE /todos/{id}

    @Test
    void deleteTodo_returns204NoContent() throws Exception {

        mockMvc.perform(delete("/todos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTodo_whenNotFound_returns404() throws Exception {
        // Since deleteTodo is void, I use doThrow to simulate the exception.
        doThrow(new TodoNotFoundException("Todo not found with id: 99"))
                .when(todoService).deleteTodo(99L);

        mockMvc.perform(delete("/todos/99"))
                .andExpect(status().isNotFound());
    }
}