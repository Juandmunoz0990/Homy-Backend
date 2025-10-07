package co.edu.uniquindio.application.Services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingFilterDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingSummaryDTO;
import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.enums.BookingStatus;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Security.CustomUserDetails;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.mappers.BookingMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repo;
    private final BookingMapper mapper;
    private final HousingRepository housingRepository;
    private final HousingService housingService;

    /**
     * Save a new booking.
     */
    @Transactional
    @Override
    public Booking save(BookingCreateDTO b) {
        boolean hasOverlap = repo.existsOverlappingBooking(
            b.getHousingId(), b.getCheckIn(), b.getCheckOut(), List.of(BookingStatus.CONFIRMED));
        if (hasOverlap) throw new IllegalStateException("The housing is not available for the selected dates.");

        if (b.getCheckIn().isAfter(b.getCheckOut())) throw new IllegalStateException("Check-in date must be before check-out date");

        if (b.getCheckOut().isBefore(LocalDate.now())) throw new IllegalStateException("Check-out date must be in the future");

        long daysBetween = ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut());
        if (daysBetween < 1) throw new IllegalStateException("Booking must be at least one night");

        Housing housing = housingRepository.findById(b.getHousingId())
        .orElseThrow(() -> new EntityNotFoundException("Housing not found"));
        if (housing.getMaxCapacity() < b.getGuestsNumber()) throw new IllegalStateException("Number of guests exceeds housing capacity");

        //Enviar correo
        return repo.save(mapper.toBooking(b));
    }

    /**
     * Cancel a booking by setting its state to CANCELED.
     */
    @Transactional
    @Override
    public void cancelBooking(Long id, Long guestId) {
        Optional<Booking> bookingOptional = repo.findByIdAndGuestId(id, guestId);

        if (!bookingOptional.isPresent()) throw new EntityNotFoundException("Booking not found for the given id and guestId.");

        Booking booking = bookingOptional.get();
        if (booking.getCheckIn().isBefore(LocalDate.now().plusDays(2))) throw new IllegalStateException("Cannot cancel a booking that has already started or is in the past.");
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) throw new IllegalStateException("Booking state must be CONFIRMED to be canceled.");

        booking.setStatus(BookingStatus.CANCELED);

        //Enviar correo
        repo.save(booking);
    }

    /**
     * Find Bookings with filters. For host or guest.
     */
    @Override  //Ver que devuelva el dto directamente
    public Page<BookingSummaryDTO> searchBookings(CustomUserDetails user, BookingFilterDTO f, Pageable pageable) {
        if (user.hasRole("HOST")) {
            //Lógica para que el host vea las reservas de solo sus alojamientos
            if (f.getHousingId() != null) {
                if (!housingService.existsByIdAndHostId(f.getHousingId(), user.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver las reservas de este alojamiento.");
                }
            }
        }
        if (user.hasRole("GUEST")) {
            //Lógica para que el huésped vea solo sus reservas
            f.setHousingId(null);
            f.setGuestId(user.getId());
        }

        return repo.searchBookings(f.getHousingId(), f.getGuestId(), f.getStatus(), f.getStart(), f.getEnd(), pageable);
    }

    /**
     * Find a booking by its id.
     */
    @Override
    public Optional<BookingDetailDTO> findBookingDetailById(Long id) {
        return repo.findBookingDetailById(id);
    }
}