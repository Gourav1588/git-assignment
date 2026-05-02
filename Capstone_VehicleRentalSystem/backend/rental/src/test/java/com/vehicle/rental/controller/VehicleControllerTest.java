package com.vehicle.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicle.rental.dto.request.VehicleRequest;
import com.vehicle.rental.dto.response.VehicleResponse;
import com.vehicle.rental.entity.Vehicle.VehicleType;
import com.vehicle.rental.repository.UserRepository;
import com.vehicle.rental.security.JwtService;
import com.vehicle.rental.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Layer tests for the {@link VehicleController}.
 * Proves that all HTTP routes correctly pass data to the VehicleService.
 */
@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypasses Spring Security for this isolated test
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    // --- Required to prevent Spring Security Context Crashes ---
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;
    // -----------------------------------------------------------

    private VehicleResponse testResponse;
    private VehicleRequest testRequest;
    private Page<VehicleResponse> testPage;

    @BeforeEach
    void setUp() {
        testResponse = new VehicleResponse();
        testResponse.setId(100L);
        testResponse.setName("Toyota Fortuner");
        testResponse.setRegistrationNumber("MP04-AB-1234");
        testResponse.setPricePerDay(4000.0);

        testRequest = new VehicleRequest();
        testRequest.setName("Toyota Fortuner");
        testRequest.setRegistrationNumber("MP04-AB-1234");
        testRequest.setType(VehicleType.CAR);
        testRequest.setPricePerDay(4000.0);
        testRequest.setCategoryId(10L);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        testPage = new PageImpl<>(List.of(testResponse), pageable, 1);
    }

    // =========================================================================
    // 1. PUBLIC READ ENDPOINTS
    // =========================================================================

    @Test
    void getPublicVehicles_Returns200AndPage() throws Exception {
        // Arrange: Notice the 'false' at the end for the isAdmin flag
        when(vehicleService.getVehicles(anyInt(), anyInt(), any(), any(), any(), eq(false)))
                .thenReturn(testPage);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Toyota Fortuner"));
    }

    @Test
    void searchAvailableVehicles_Returns200AndList() throws Exception {
        // Arrange
        when(vehicleService.findAvailableVehicles(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testResponse));

        // Act & Assert: We pass ISO formatted date strings just like the frontend would
        mockMvc.perform(get("/api/vehicles/search")
                        .param("startTime", "2026-05-01T10:00:00")
                        .param("endTime", "2026-05-05T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Toyota Fortuner"));
    }

    @Test
    void getVehicleById_Returns200AndVehicle() throws Exception {
        // Arrange
        when(vehicleService.getVehicleById(100L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toyota Fortuner"));
    }

    // =========================================================================
    // 2. SECURE ADMIN ENDPOINTS
    // =========================================================================

    @Test
    void getAdminVehicles_Returns200AndPage() throws Exception {
        // Arrange: Notice the 'true' at the end for the isAdmin flag
        when(vehicleService.getVehicles(anyInt(), anyInt(), any(), any(), any(), eq(true)))
                .thenReturn(testPage);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Toyota Fortuner"));
    }

    // =========================================================================
    // 3. SECURE MUTATION ENDPOINTS (WRITE OPERATIONS)
    // =========================================================================

    @Test
    void createVehicle_Returns200AndVehicle() throws Exception {
        // Arrange
        when(vehicleService.createVehicle(any(VehicleRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toyota Fortuner"));
    }

    @Test
    void updateVehicle_Returns200AndVehicle() throws Exception {
        // Arrange
        when(vehicleService.updateVehicle(eq(100L), any(VehicleRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toyota Fortuner"));
    }

    @Test
    void toggleVehicleStatus_Returns200AndVehicle() throws Exception {
        // Arrange
        when(vehicleService.toggleVehicleStatus(100L)).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/100/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toyota Fortuner"));
    }
}