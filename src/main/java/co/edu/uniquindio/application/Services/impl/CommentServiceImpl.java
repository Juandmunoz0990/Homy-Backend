package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.enums.BookingStatus;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.CommentService;
import co.edu.uniquindio.application.mappers.CommentMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repo;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;
    private final HousingRepository housingRepository;

    public CommentServiceImpl(CommentRepository repo, CommentMapper commentMapper, 
                              BookingRepository bookingRepository, HousingRepository housingRepository) {
        this.repo = repo;
        this.commentMapper = commentMapper;
        this.bookingRepository = bookingRepository;
        this.housingRepository = housingRepository;
    }

    @Override
    public EntityCreatedResponse create(Long guestId, CommentRequest c) {
        // Validar que la reserva existe y pertenece al huésped
        Booking booking = bookingRepository.findById(c.getBookingId())
            .orElseThrow(() -> new IllegalStateException("Reserva no encontrada"));
        
        if (!booking.getGuest().getId().equals(guestId)) {
            throw new IllegalStateException("No tienes permiso para comentar esta reserva");
        }
        
        // Validar que el check-out ya pasó (estadía completada)
        LocalDate today = LocalDate.now();
        if (booking.getCheckOut().isAfter(today) || booking.getCheckOut().isEqual(today)) {
            throw new IllegalStateException("Solo puedes comentar después de completar tu estadía (después del check-out)");
        }
        
        // Validar que la reserva esté completada
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Solo puedes comentar reservas completadas");
        }
        
        // Validar que no exista ya un comentario para esta reserva (máximo 1 por reserva)
        if (repo.findByBookingIdAndGuestId(c.getBookingId(), guestId).isPresent()) {
            throw new IllegalStateException("Ya has comentado esta reserva. Solo se permite un comentario por reserva");
        }
        
        // Validar que el housingId coincida con el de la reserva
        if (!booking.getHousing().getId().equals(c.getHousingId())) {
            throw new IllegalStateException("El alojamiento no coincide con la reserva");
        }
        
        Housing housing = housingRepository.findById(c.getHousingId())
            .orElseThrow(() -> new ObjectNotFoundException("Alojamiento no encontrado", Housing.class));
        
        Comment comment = commentMapper.toComment(c);
        comment.setGuestId(guestId);
        comment.setHousing(housing);
        repo.save(comment);
        
        // Actualizar promedio de calificaciones del alojamiento
        updateHousingAverageRating(c.getHousingId());
        
        return new EntityCreatedResponse("Comentario creado exitosamente", Instant.now());
    }
    
    /**
     * Actualiza el promedio de calificaciones de un alojamiento
     */
    private void updateHousingAverageRating(Long housingId) {
        List<Comment> comments = repo.findByHousingIdOrderByCreatedAtDesc(housingId);
        if (comments.isEmpty()) {
            return;
        }
        
        double average = comments.stream()
            .mapToInt(Comment::getRate)
            .average()
            .orElse(0.0);
        
        Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Alojamiento no encontrado", Housing.class));
        housing.setAverageRating(average);
        housingRepository.save(housing);
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