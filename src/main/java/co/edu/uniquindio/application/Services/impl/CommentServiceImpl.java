package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Services.CommentService;
import co.edu.uniquindio.application.mappers.CommentMapper;

import java.time.Instant;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repo;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository repo, CommentMapper commentMapper) {
        this.repo = repo;
        this.commentMapper = commentMapper;
    }

    @Override
    public EntityCreatedResponse create(Long guestId, CommentRequest c) {
        Comment comment = commentMapper.toComment(c);
        comment.setGuestId(guestId);
        repo.save(comment);
        return new EntityCreatedResponse("Comment created succesfully", Instant.now());
    }

    @Override
    public List<CommentResponse> findByHousingId(Long housingId) {
       List<Comment> commentsList = repo.findByHousingIdOrderByCreatedAtDesc(housingId);
       List<CommentResponse> comments = commentMapper.toList(commentsList);
       return comments;
    }

    @Override
    public void replyComment(Long hostId, Long commentId, String message) {
        Comment comment = repo.findById(commentId).orElseThrow(() -> new ObjectNotFoundException("Comment with id: " + commentId + " not found", Comment.class));
        comment.setHostReply(message);
        repo.save(comment);
    }

    
}