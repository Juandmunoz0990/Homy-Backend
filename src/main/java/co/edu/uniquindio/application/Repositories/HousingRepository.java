package co.edu.uniquindio.application.Repositories;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.Housing;

@Repository
public interface HousingRepository extends JpaRepository<Housing, Long> {

    @Query("""
        SELECT h 
        FROM Housing h 
        WHERE (:city IS NULL OR LOWER(h.city) = LOWER(:city))
          AND (:totalGuests IS NULL OR h.maxCapacity >= :totalGuests)
          AND h.state = 'active'
          AND NOT EXISTS (
                SELECT b 
                FROM Booking b 
                WHERE b MEMBER OF h.bookingsList
                  AND (
                       (b.checkIn <= :checkOut AND b.checkOut >= :checkIn)
                  )
          )
        """)
    Page<Housing> findHousingsByFilters(@Param("city") String city,
                                        @Param("checkIn") LocalDate checkIn,
                                        @Param("checkOut") LocalDate checkOut,
                                        @Param("totalGuests") Integer totalGuests,
                                        Pageable pageable);

    Boolean existsByIdAndHostId(Long housingId, Long hostId);
}
