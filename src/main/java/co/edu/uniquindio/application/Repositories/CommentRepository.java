package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.application.Models.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByHousingIdOrderByCreatedAtDesc(Long alojamientoId);
}