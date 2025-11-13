package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Models.enums.BookingStatus;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Services.CommentService;
import co.edu.uniquindio.application.Services.EmailService;
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.CommentMapper;
import co.edu.uniquindio.application.Dtos.email.EmailDTO;

import java.time.Instant;
import java.util.List;
import java.time.LocalDate;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repo;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;
    private final HousingRepository housingRepository;
    private final EmailService emailService;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository repo, CommentMapper commentMapper,
                              BookingRepository bookingRepository, HousingRepository housingRepository,
                              EmailService emailService, UserService userService) {
        this.repo = repo;
        this.commentMapper = commentMapper;
        this.bookingRepository = bookingRepository;
        this.housingRepository = housingRepository;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Override
    public EntityCreatedResponse create(Long guestId, CommentRequest c) {
        var booking = bookingRepository.findById(c.getBookingId())
                .orElseThrow(() -> new ObjectNotFoundException("Booking with id: " + c.getBookingId() + " not found", co.edu.uniquindio.application.Models.Booking.class));

        if (!booking.getGuest().getId().equals(guestId)) {
            throw new IllegalArgumentException("Solo puedes comentar reservas que te pertenecen");
        }

        if (!booking.getHousing().getId().equals(c.getHousingId())) {
            throw new IllegalArgumentException("La reserva no pertenece al alojamiento seleccionado");
        }

        if (booking.getCheckOut().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Solo puedes comentar después de la fecha de salida");
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalStateException("No es posible comentar reservas canceladas");
        }

        if (repo.existsByBookingId(c.getBookingId())) {
            throw new IllegalStateException("Ya existe un comentario para esta reserva");
        }

        Housing housing = housingRepository.findById(c.getHousingId())
                .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + c.getHousingId() + " not found", Housing.class));

        Comment comment = commentMapper.toComment(c);
        comment.setGuestId(guestId);
        comment.setHousing(housing);
        repo.save(comment);

        Double newAverage = repo.calculateAverageByHousing(housing.getId());
        housing.setAverageRating(newAverage != null ? newAverage : 0.0);
        housingRepository.save(housing);

        User host = userService.findById(housing.getHostId());
        User guest = booking.getGuest();
        try {
            emailService.sendMail(new EmailDTO(
                    "Nuevo comentario recibido",
                    "Has recibido una nueva reseña de " + guest.getName() +
                            " para tu alojamiento " + housing.getTitle() +
                            ". Calificación: " + c.getRate() + " estrellas.",
                    host.getEmail()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send comment notification", e);
        }
        return new EntityCreatedResponse("Comment created succesfully", Instant.now());
    }

    @Override
    public List<CommentResponse> findByHousingId(Long housingId) {
       List<Comment> commentsList = repo.findByHousingIdOrderByCreatedAtDesc(housingId);
       return commentsList.stream().map(comment -> {
           CommentResponse response = commentMapper.toResponse(comment);
           response.setGuestName(userService.findById(comment.getGuestId()).getName());
           response.setHousingTitle(comment.getHousing().getTitle());
           return response;
       }).toList();
    }

    @Override
    public void replyComment(Long hostId, Long commentId, String message) {
        Comment comment = repo.findById(commentId).orElseThrow(() -> new ObjectNotFoundException("Comment with id: " + commentId + " not found", Comment.class));
        if (!comment.getHousing().getHostId().equals(hostId)) {
            throw new IllegalArgumentException("No tienes permiso para responder este comentario");
        }
        comment.setHostReply(message);
        repo.save(comment);
    }


}