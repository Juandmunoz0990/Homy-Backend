package co.edu.uniquindio.application.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Models.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "guestId", ignore = true)
    @Mapping(target = "hostReply", ignore = true)
    @Mapping(target = "housing", ignore = true)
    Comment toComment (CommentRequest request);

    List<CommentResponse> toList (List<Comment> comments);
}
