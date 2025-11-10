package co.edu.uniquindio.application.Repositories;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.Housing;

@Repository
public interface HousingRepository extends JpaRepository<Housing, Long> {

  @Query("""
    SELECT DISTINCT h
    FROM Housing h
    WHERE (:city IS NULL OR LOWER(h.city) = LOWER(:city))
      AND (:minPrice IS NULL OR h.nightPrice >= :minPrice)
      AND (:maxPrice IS NULL OR h.nightPrice <= :maxPrice)
      AND h.state = 'active'
      AND NOT EXISTS (
          SELECT b FROM Booking b
          WHERE b.housing = h
            AND b.status = 'CONFIRMED'
            AND b.checkIn < :checkOut
            AND b.checkOut > :checkIn
      )
    """)
Page<Housing> findHousingsByFilters(
        @Param("city") String city,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
);

  Boolean existsByIdAndHostId(Long housingId, Long hostId);

  @Query("SELECT h FROM Housing h WHERE h.hostId = :hostId AND h.state = 'active' ORDER BY h.id DESC")
  Page<Housing> findByHostId(@Param("hostId") Long hostId, Pageable pageable);

  @Modifying
  @Query("UPDATE Housing h SET h.state = 'deleted' WHERE h.id = :housingId AND h.hostId = :hostId")
  void softDeleteByIdAndHostId(@Param("housingId") Long housingId, @Param("hostId") Long hostId);
}
