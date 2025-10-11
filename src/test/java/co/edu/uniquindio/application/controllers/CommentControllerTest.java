package co.edu.uniquindio.application.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Services.CommentService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    // ---------- LIST COMMENTS ----------
    @Test
    @Sql("classpath:dataset.sql")
    void testListCommentsSuccess() throws Exception {
        Long housingId = 1L;

        CommentResponse response = new CommentResponse();
        response.setGuestName("Juan");
        response.setHousingTitle("Alojamiento Centro");
        response.setRate(5);
        response.setContent("Excelente lugar");
        response.setHostReply("Gracias por tu comentario");
        response.setCreatedAt(LocalDateTime.now());

        Mockito.when(commentService.findByHousingId(housingId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/housings/{housingId}/comments", housingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestName").value("Juan"))
                .andExpect(jsonPath("$[0].content").value("Excelente lugar"));
    }

    // ---------- CREATE COMMENT ----------
    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"GUEST"})
    void testCreateCommentSuccess() throws Exception {
        Long housingId = 1L;

        CommentRequest request = new CommentRequest();
        request.setHousingId(housingId);
        request.setContent("Muy bonito y limpio");
        request.setRate(5);

        EntityCreatedResponse response =
                new EntityCreatedResponse("Comentario creado correctamente", Instant.now());

        Mockito.when(commentService.create(anyLong(), any(CommentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/housings/{housingId}/comments/create", housingId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Comentario creado correctamente"));
    }

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"HOST"})
    void testCreateCommentForbiddenForHost() throws Exception {
        Long housingId = 1L;

        CommentRequest request = new CommentRequest();
        request.setHousingId(housingId);
        request.setContent("Intento de host");
        request.setRate(4);

        mockMvc.perform(post("/housings/{housingId}/comments/create", housingId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ---------- REPLY COMMENT ----------
    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "1", authorities = {"HOST"})
    void testReplyCommentSuccess() throws Exception {
        Long commentId = 1L;
        Long housingId = 1L;
        String replyMessage = "Gracias por tu opinión";

        mockMvc.perform(post("/housings/{housingId}/comments/{commentId}", housingId, commentId)
                        .param("message", replyMessage))
                .andExpect(status().isOk());

        Mockito.verify(commentService)
                .replyComment(eq(1L), eq(commentId), eq(replyMessage));
    }

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "2", authorities = {"GUEST"})
    void testReplyCommentForbiddenForGuest() throws Exception {
        Long housingId = 1L;
        Long commentId = 1L;

        mockMvc.perform(post("/housings/{housingId}/comments/{commentId}", housingId, commentId)
                        .param("message", "Intento no permitido"))
                .andExpect(status().isForbidden());
    }
}
