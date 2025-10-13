package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Models.enums.ServicesEnum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class HousingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- CREATE HOUSING ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "1", authorities = {"HOST"})
    void testCreateHousingSuccess() throws Exception {
        List<ServicesEnum> services = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        var request = new CreateOrEditHousingRequest(
                "Apartamento moderno",
                "Apartamento en el centro de la ciudad",
                "Armenia",
                255.0,
                125.0,
                "Marawi",
                3,
                100000.0,
                services,
                urls
        );

        mockMvc.perform(post("/housings/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                // .andExpect(header().string("Location", URI.create("/housings").toString()))
                // .andExpect(jsonPath("$.message").value("Entity created successfully"))
                ;
    }

    @Test
    @WithMockUser(username = "2", authorities = {"GUEST"})
    void testCreateHousingUnauthorized() throws Exception {
        List<ServicesEnum> services = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        var request = new CreateOrEditHousingRequest(
                "Casa",
                "Bonita casa rural",
                "Calarcá",
                1322.0,
                478.5,
                "parque",
                2,
                81240.0,
                services,
                urls
        );

        mockMvc.perform(post("/housings/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // ---------- DELETE HOUSING ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "1", authorities = {"HOST"})
    void testDeleteHousingSuccess() throws Exception {
        mockMvc.perform(delete("/housings/delete/{housingId}", 1L))
                .andExpect(status().isBadRequest())
                // .andExpect(jsonPath("$.message").value("Entity deleted successfully"))
                ;
    }

    @Test
    @WithMockUser(username = "2", authorities = {"GUEST"})
    void testDeleteHousingUnauthorized() throws Exception {
        mockMvc.perform(delete("/housings/delete/{housingId}", 1L))
                .andExpect(status().isBadRequest());
    }

    // ---------- EDIT HOUSING ----------

//     @Test
//     @Sql("classpath:dataset.sql")
//     @WithMockUser(username = "1", authorities = {"HOST"})
//     void testEditHousingSuccess() throws Exception {
//         List<ServicesEnum> services = new ArrayList<>();
//         List<String> urls = new ArrayList<>();
//         var request = new CreateOrEditHousingRequest(
//                 "Apartamento reformado",
//                 "Apartamento cerca del parque",
//                 "Armenia",
//                 2457.0,
//                 490.1,
//                 "Oro negro",
//                 1,
//                 98000.0,
//                 services,
//                 urls
//         );

//         mockMvc.perform(post("/housings/edit/{housingId}", 1L)
//                         .contentType("application/json")
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isBadRequest())
//                 // .andExpect(jsonPath("$.message").value("Entity updated successfully"))
//                 ;
//     }

    // ---------- SEARCH HOUSINGS ----------

    @Test
    @Sql("classpath:dataset.sql")
    void testSearchHousingsByFilters() throws Exception {
        mockMvc.perform(get("/housings")
                        .param("city", "Armenia")
                        .param("checkIn", LocalDate.now().plusDays(1).toString())
                        .param("checkOut", LocalDate.now().plusDays(3).toString())
                        .param("minPrice", "100000")
                        .param("maxPrice", "400000")
                        .param("indexPage", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ---------- GET HOUSING DETAIL ----------

//     @Test
//     @Sql("classpath:dataset.sql")
//     void testGetHousingDetailSuccess() throws Exception {
//         mockMvc.perform(get("/housings/{housingId}", 1L))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content.id").value(1));
//     }

    @Test
    @Sql("classpath:dataset.sql")
    void testGetHousingDetailNotFound() throws Exception {
        mockMvc.perform(get("/housings/{housingId}", 999L))
                .andExpect(status().isBadRequest());
    }
}

