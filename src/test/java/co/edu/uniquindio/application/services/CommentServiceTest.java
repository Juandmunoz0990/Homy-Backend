package co.edu.uniquindio.application.services;


import jakarta.transaction.Transactional;

import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Services.CommentService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    // ------------------- Tests usando dataset.sql -------------------

    /**
     * Test crear comentario
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testCreateComment() {
        Long guestId = 2L;

        CommentRequest request = new CommentRequest();
        request.setHousingId(1L);
        request.setContent("Excelente alojamiento, volveré pronto!");

        EntityCreatedResponse response = commentService.create(guestId, request);

        assertNotNull(response);
        assertEquals("Comment created succesfully", response.getMessage());
        assertTrue(response.getTimestamp().isBefore(Instant.now()));
    }

    /**
     * Test obtener comentarios por housingId
     */
    // @Test
    // @Sql("classpath:dataset.sql")
    // void testFindByHousingId() {
    //     Long housingId = 1L;

    //     List<CommentResponse> comments = commentService.findByHousingId(housingId);

    //     assertNotNull(comments);
    //     assertFalse(comments.isEmpty());
    //     assertTrue(comments.stream().allMatch(c -> c.getHousingId().equals(housingId)));
    // }

    /**
     * Test responder comentario existente
     */
    // @Test
    // @Sql("classpath:dataset.sql")
    // void testReplyComment() {
    //     Long hostId = 1L;
    //     Long commentId = 1L;
    //     String message = "Gracias por tu comentario!";

    //     assertDoesNotThrow(() -> commentService.replyComment(hostId, commentId, message));
    // }

    /**
     * Test responder comentario inexistente → debe lanzar excepción
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testReplyComment_notFound_thenThrows() {
        Long hostId = 1L;
        Long invalidCommentId = 999L;

        assertThrows(ObjectNotFoundException.class, () ->
                commentService.replyComment(hostId, invalidCommentId, "Respuesta no válida"));
    }
}


