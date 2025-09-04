package co.edu.uniquindio.application.Services.impl;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Services.CommentService;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repo;

    public CommentServiceImpl(CommentRepository repo) {
        this.repo = repo;
    }

    @Override
    public Comment create(Comment c) {
        return repo.save(c);
    }

    @Override
    public List<Comment> findByHousingId(Long alojamientoId) {
        return repo.findByHousingIdOrderByCreatedAtDesc(alojamientoId);
    }
}