package co.edu.uniquindio.application.Services;

import java.util.List;

import co.edu.uniquindio.application.Models.Comment;

public interface CommentService {
    Comment create(Comment c);

    List<Comment> findByHousingId(Long alojamientoId);
}