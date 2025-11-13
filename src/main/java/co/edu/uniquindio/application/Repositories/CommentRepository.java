package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.application.Models.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByHousingIdOrderByCreatedAtDesc(Long housingId);

    boolean existsByBookingId(Long bookingId);

    @Query("""
        SELECT AVG(c.rate)
        FROM Comment c
        WHERE c.housing.id = :housingId
    """)
    Double calculateAverageByHousing(@Param("housingId") Long housingId);
}