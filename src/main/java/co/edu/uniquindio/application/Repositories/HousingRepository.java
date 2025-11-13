package co.edu.uniquindio.application.Repositories;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.enums.ServicesEnum;

@Repository
public interface HousingRepository extends JpaRepository<Housing, Long> {

  @Query("""
    SELECT h
    FROM Housing h
    LEFT JOIN h.services svc
    WHERE h.state = 'active'
      AND (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
      AND (:minPrice IS NULL OR h.nightPrice >= :minPrice)
      AND (:maxPrice IS NULL OR h.nightPrice <= :maxPrice)
      AND (:totalGuests IS NULL OR h.maxCapacity >= :totalGuests)
      AND ((:checkIn IS NULL OR :checkOut IS NULL) OR NOT EXISTS (
            SELECT b FROM Booking b
            WHERE b.housing = h
              AND b.status = 'CONFIRMED'
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
      ))
    GROUP BY h
    HAVING (:servicesCount = 0 OR COUNT(DISTINCT CASE WHEN svc IN :services THEN svc END) = :servicesCount)
    """)
Page<Housing> findHousingsByFilters(
        @Param("city") String city,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("totalGuests") Integer totalGuests,
        @Param("services") List<ServicesEnum> services,
        @Param("servicesCount") Integer servicesCount,
        Pageable pageable
);

  @Query("""
    SELECT h
    FROM Housing h
    WHERE h.state = 'active'
      AND (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
      AND (:minPrice IS NULL OR h.nightPrice >= :minPrice)
      AND (:maxPrice IS NULL OR h.nightPrice <= :maxPrice)
      AND (:totalGuests IS NULL OR h.maxCapacity >= :totalGuests)
      AND ((:checkIn IS NULL OR :checkOut IS NULL) OR NOT EXISTS (
            SELECT b FROM Booking b
            WHERE b.housing = h
              AND b.status = 'CONFIRMED'
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
      ))
    """)
  Page<Housing> findHousingsByFilters(
        @Param("city") String city,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("totalGuests") Integer totalGuests,
        Pageable pageable
  );

  Boolean existsByIdAndHostIdAndStateNot(Long housingId, Long hostId, String state);

  @Query("SELECT h FROM Housing h WHERE h.hostId = :hostId AND h.state = 'active' ORDER BY h.id DESC")
  Page<Housing> findByHostId(@Param("hostId") Long hostId, Pageable pageable);

  @Modifying
  @Query("UPDATE Housing h SET h.state = 'deleted' WHERE h.id = :housingId AND h.hostId = :hostId")
  void softDeleteByIdAndHostId(@Param("housingId") Long housingId, @Param("hostId") Long hostId);

  @Query("""
      SELECT COUNT(b)
      FROM Booking b
      WHERE b.housing.id = :housingId
        AND b.status <> 'CANCELED'
        AND (:startDate IS NULL OR b.checkIn >= :startDate)
        AND (:endDate IS NULL OR b.checkOut <= :endDate)
    """)
  Long countBookingsForHousing(@Param("housingId") Long housingId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

  @Query("""
      SELECT AVG(c.rate)
      FROM Comment c
      WHERE c.housing.id = :housingId
        AND (:startDate IS NULL OR c.createdAt >= :startDate)
        AND (:endDate IS NULL OR c.createdAt <= :endDate)
    """)
  Double calculateAverageRating(@Param("housingId") Long housingId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
