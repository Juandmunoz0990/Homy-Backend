package co.edu.uniquindio.application.Services;

import java.util.List;


import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;

public interface CommentService {
    EntityCreatedResponse create(Long guestId, CommentRequest c);

    List<CommentResponse> findByHousingId(Long housingId);

    void replyComment(Long hostId, Long commentId, String message);
}