package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.enums.BookingStatus;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Services.CommentService;
import co.edu.uniquindio.application.mappers.CommentMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repo;
    private final BookingRepository bookingRepository;
    private final HousingRepository housingRepository;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository repo, BookingRepository bookingRepository,
                              HousingRepository housingRepository, CommentMapper commentMapper) {
        this.repo = repo;
        this.bookingRepository = bookingRepository;
        this.housingRepository = housingRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public EntityCreatedResponse create(Long guestId, CommentRequest c) {
        Booking booking = bookingRepository.findById(c.getBookingId())
            .orElseThrow(() -> new ObjectNotFoundException("Booking with id: " + c.getBookingId() + " not found", Booking.class));

        if (!booking.getGuest().getId().equals(guestId)) {
            throw new IllegalStateException("Solo puedes calificar reservaciones que te pertenecen.");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Solo puedes comentar reservas completadas.");
        }

        if (!booking.getCheckOut().isBefore(LocalDate.now()) && !booking.getCheckOut().isEqual(LocalDate.now())) {
            throw new IllegalStateException("La reserva aún no ha finalizado.");
        }

        if (!booking.getHousing().getId().equals(c.getHousingId())) {
            throw new IllegalArgumentException("El alojamiento no coincide con la reserva proporcionada.");
        }

        if (repo.existsByBookingId(c.getBookingId())) {
            throw new IllegalStateException("Ya registraste un comentario para esta reserva.");
        }

        Comment comment = commentMapper.toComment(c);
        comment.setGuestId(guestId);
        comment.setHousing(booking.getHousing());
        comment.setBookingId(booking.getId());
        repo.save(comment);

        Housing housing = booking.getHousing();
        Double averageRating = repo.findAverageRateByHousingId(housing.getId());
        housing.setAverageRating(averageRating != null ? averageRating : 0.0);
        housingRepository.save(housing);
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