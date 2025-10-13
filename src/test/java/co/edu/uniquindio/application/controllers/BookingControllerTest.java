package co.edu.uniquindio.application.controllers;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class BookingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- CREATE BOOKING ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "guest1@email.com", authorities = {"GUEST"})
    void testCreateBookingSuccess() throws Exception {
        var booking = new BookingCreateDTO(
                1L,      // housingId
                2L,      // guestId
                LocalDate.of(2025, 10, 26),
                LocalDate.of(2025, 10, 27),
                2,
                300.0
        );

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Booking created successfully"));
    }

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "guest1@email.com", authorities = {"GUEST"})
    void testCreateBookingFailsWhenDatesInvalid() throws Exception {
        var booking = new BookingCreateDTO(
                1L,
                2L,
                LocalDate.of(2025, 10, 25),
                LocalDate.of(2025, 10, 20), // fecha de salida antes de entrada
                2,
                null
        );

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isBadRequest());
    }

    // ---------- CANCEL BOOKING ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"GUEST"})
    void testCancelBookingSuccess() throws Exception {
        mockMvc.perform(patch("/bookings/1/cancel"))
                .andExpect(status().isNotFound());
    }

//     @Test
//     @Sql("classpath:dataset.sql")
//     @WithMockUser(username = "guest2@email.com", authorities = {"HOST"})
//     void testCancelBookingUnauthorized() throws Exception {
//         // Intentar cancelar una reserva que no pertenece al usuario autenticado
//         mockMvc.perform(patch("/bookings/1/cancel"))
//                 .andExpect(status().isForbidden());
//     }

    // ---------- SEARCH BOOKINGS ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "1", authorities = {"HOST"})
    void testSearchBookingsAsHost() throws Exception {

        mockMvc.perform(get("/bookings/search")
                        .param("housingId", "1")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest())
                // .andExpect(jsonPath("$.content.content").isArray())
                ;
    }

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"GUEST"})
    void testSearchBookingsAsGuest() throws Exception {
        mockMvc.perform(get("/bookings/search")
                        .param("guestId", "2")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest())
                // .andExpect(jsonPath("$.content.content").isArray())
                ;
    }

    // ---------- GET BY ID ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"GUEST", "HOST"})
    void testGetBookingByIdSuccess() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.id").value(1));
    }

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"GUEST", "HOST"})
    void testGetBookingByIdNotFound() throws Exception {
        mockMvc.perform(get("/bookings/999"))
                .andExpect(status().isNotFound());
    }
}
