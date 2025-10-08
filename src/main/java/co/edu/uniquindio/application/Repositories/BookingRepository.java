package co.edu.uniquindio.application.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingSummaryDTO;
import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Models.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByHousingId(Long alojamientoId);

    /**
     * Finds all bookings that belong to a given host.
     *
     * @param hostId the unique identifier of the host
     * @return a list of bookings associated with the host’s housings
     */
    List<Booking> findByHousingHostId(Long hostId);

    /**
     * Finds all bookings with filters.
     *
     * @param housingId the unique identifier of the housing -> when host wants to see all bookings for a specific housing
     * @param guestId the unique identifier of the guest -> when guest wants to see all their bookings
     * @param status the status of the booking
     * @param start the check-in date of the booking
     * @param end the check-out date of the booking
     * @return a list of bookings associated with the filters
     */
    @Query("""
    SELECT new co.edu.uniquindio.application.Dtos.booking.BookingSummaryDTO(
        b.id,
        h.title,
        h.principalImage,
        h.city,
        b.checkIn,
        b.checkOut,
        b.guestsNumber,
        b.status,
        b.totalPrice,
        g.name
        )
        FROM Booking b
        JOIN b.housing h
        JOIN b.guest g
        WHERE (:housingId IS NULL OR h.id = :housingId)
          AND (:guestId IS NULL OR g.id = :guestId)
          AND (:status IS NULL OR b.status = :status)
          AND (:start IS NULL OR b.checkIn >= :start)
          AND (:end IS NULL OR b.checkOut <= :end)
    """)
    Page<BookingSummaryDTO> searchBookings(
        @Param("housingId") Long housingId,
        @Param("guestId") Long guestId,
        @Param("status") BookingStatus status,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end,
        Pageable pageable
    );

    @Query("""
    SELECT new co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO(
        b.id,
        b.checkIn,
        b.checkOut,
        b.guestsNumber,
        b.status,
        b.totalPrice,
        b.createdAt,
        new com.example.booking.dto.BookingDetailDTO$HousingInfo(
            h.id, h.title, h.description, h.address, h.city,
            h.nightPrice, h.maxCapacity, h.principalImage, h.averageRating
        ),
        new com.example.booking.dto.BookingDetailDTO$GuestInfo(
            g.id, g.name, g.email, g.phoneNumber
        )
        )
        FROM Booking b
        JOIN b.housing h
        JOIN b.guest g
        WHERE b.id = :id
    """)
    Optional<BookingDetailDTO> findBookingDetailById(@Param("id") Long id);


    Optional<Booking> findByIdAndGuestId(Long id, Long guestId);

    @Query("""
    SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
    FROM Booking b
    WHERE b.housing.id = :housingId
      AND b.status IN :statuses
      AND (:startDate < b.checkOut AND :endDate > b.checkIn)
    """)
    boolean existsOverlappingBooking(
        @Param("housingId") Long housingId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("statuses") List<BookingStatus> statuses);

    /**
     * Returns true if there exists at least one booking for the given housing
     * with a status in the provided list and with checkOut >= :today
     * (meaning the booking is active or in the future).
     */
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
        FROM Booking b
        WHERE b.housing.id = :housingId
          AND b.status IN :statuses
          AND b.checkOut >= :today
    """)
    boolean existsActiveOrFutureBookingForHousing(
        @Param("housingId") Long housingId,
        @Param("statuses") List<BookingStatus> statuses,
        @Param("today") LocalDate today
    );
}