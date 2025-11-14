package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.application.Models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByHousingIdOrderByCreatedAtDesc(Long housingId);
    
    // Verificar si ya existe un comentario para una reserva
    Optional<Comment> findByBookingId(Long bookingId);
    
    // Verificar si existe comentario para booking y guest
    @Query("SELECT c FROM Comment c WHERE c.bookingId = :bookingId AND c.guestId = :guestId")
    Optional<Comment> findByBookingIdAndGuestId(@Param("bookingId") Long bookingId, @Param("guestId") Long guestId);
    
    /**
     * Calculate average rating for a housing within a date range
     */
    @Query("""
        SELECT AVG(c.rate)
        FROM Comment c
        WHERE c.housing.id = :housingId
          AND (:dateFrom IS NULL OR c.createdAt >= :dateFrom)
          AND (:dateTo IS NULL OR c.createdAt <= :dateTo)
    """)
    Double calculateAverageRatingByHousingAndDateRange(
        @Param("housingId") Long housingId,
        @Param("dateFrom") java.time.LocalDateTime dateFrom,
        @Param("dateTo") java.time.LocalDateTime dateTo
    );
    
    /**
     * Get all comments for a housing to calculate average manually if needed
     */
    @Query("SELECT c FROM Comment c WHERE c.housing.id = :housingId")
    List<Comment> findAllByHousingId(@Param("housingId") Long housingId);
}