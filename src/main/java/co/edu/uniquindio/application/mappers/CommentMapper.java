package co.edu.uniquindio.application.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Models.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toComment (CommentRequest request);
    List<CommentResponse> toList (List<Comment> comments);
    
}
