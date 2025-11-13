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
import co.edu.uniquindio.application.Dtos.booking.GuestInfo;
import co.edu.uniquindio.application.Dtos.booking.HousingInfo;
import co.edu.uniquindio.application.Dtos.email.EmailDTO;
import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Models.enums.BookingStatus;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.EmailService;
import co.edu.uniquindio.application.Services.UserService;
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
    private final EmailService emailService;
    private final UserService userService;

    /**
     * Save a new booking.
     */
    @Override
    @Transactional
    public Booking save(BookingCreateDTO b, Long guestId) {
        boolean hasOverlap = repo.existsOverlappingBooking(
            b.housingId(), b.checkIn(), b.checkOut(), List.of(BookingStatus.CONFIRMED));
        if (hasOverlap) throw new IllegalStateException("The housing is not available for the selected dates.");

        if (b.checkIn().isAfter(b.checkOut())) throw new IllegalStateException("Check-in date must be before check-out date");

        if (b.checkOut().isBefore(LocalDate.now())) throw new IllegalStateException("Check-out date must be in the future");

        long daysBetween = ChronoUnit.DAYS.between(b.checkIn(), b.checkOut());
        if (daysBetween < 1) throw new IllegalStateException("Booking must be at least one night");

        Long housingId = b.housingId();
        if (housingId == null) throw new IllegalArgumentException("Housing id is required");
        Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new EntityNotFoundException("Housing not found"));
        if (housing.getMaxCapacity() < b.guestsNumber()) throw new IllegalStateException("Number of guests exceeds housing capacity");

        User guest = userService.findById(guestId);
        //Enviar correo
        try {
            emailService.sendMail(new EmailDTO(
                "Reserva creada",
                "Su reserva ha sido creada exitosamente." + "\nDetalles:\nAlojamiento: " + housing.getTitle() +
                "\nCheck-in: " + b.checkIn() + "\nCheck-out: " + b.checkOut() +
                "\nNúmero de huéspedes: " + b.guestsNumber() + "\nPrecio total: " + b.totalPrice(),
                guest.getEmail()));

            User host = userService.findById(housing.getHostId());
            emailService.sendMail(new EmailDTO(
                "Nueva reserva",
                "Has recibido una nueva reserva para " + housing.getTitle() +
                ".\nHuésped: " + guest.getName() +
                "\nCheck-in: " + b.checkIn() + "\nCheck-out: " + b.checkOut() +
                "\nNúmero de huéspedes: " + b.guestsNumber(),
                host.getEmail()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
        Booking booking = mapper.toBooking(b);
        booking.setHousing(housing);
        booking.setGuest(guest);

        return repo.save(booking);
    }

    /**
     * Cancel a booking by setting its state to CANCELED.
     */
    @Override
    @Transactional
    public void cancelBooking(Long id, Long guestId) {
        Optional<Booking> bookingOptional = repo.findByIdAndGuestId(id, guestId);

        if (!bookingOptional.isPresent()) throw new EntityNotFoundException("Booking not found for the given id and guestId.");

        Booking booking = bookingOptional.get();
        if (booking.getCheckIn().isBefore(LocalDate.now().plusDays(2))) throw new IllegalStateException("Cannot cancel a booking that has already started or is in the past.");
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) throw new IllegalStateException("Booking state must be CONFIRMED to be canceled.");

        booking.setStatus(BookingStatus.CANCELED);

        User guest = userService.findById(guestId);
        //Enviar correo
        try {
            emailService.sendMail(new EmailDTO(
                "Reserva cancelada",
                "Tu reserva ha sido cancelada exitosamente." + "\nDetalles:\nAlojamiento: " + booking.getHousing().getTitle() +
                "\nCheck-in: " + booking.getCheckIn() + "\nCheck-out: " + booking.getCheckOut() +
                "\nNúmero de huéspedes: " + booking.getGuestsNumber() + "\nPrecio total: " + booking.getTotalPrice(),
                guest.getEmail()));

            User host = userService.findById(booking.getHousing().getHostId());
            emailService.sendMail(new EmailDTO(
                "Reserva cancelada",
                "El huésped " + guest.getName() + " canceló la reserva para " + booking.getHousing().getTitle() +
                ".\nCheck-in previsto: " + booking.getCheckIn(),
                host.getEmail()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
        repo.save(booking);
    }

    /**
     * Find Bookings with filters. For host or guest.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookingSummaryDTO> searchBookings(org.springframework.security.core.userdetails.User user, BookingFilterDTO f, Pageable pageable) {
        Long userId = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        if (user.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("HOST"))) {
            //Lógica para que el host vea las reservas de solo sus alojamientos
            if (f.housingId() != null) {
                if (!housingRepository.existsByIdAndHostIdAndStateNot(f.housingId(), userId, Housing.STATE_DELETED)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver las reservas de este alojamiento.");
                }
            }
        }
        if (user.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("GUEST"))) {
            //Lógica para que el huésped vea solo sus reservas
            f = new BookingFilterDTO(null, userId, f.status(), f.start(), f.end());
        }
        return repo.searchBookings(f.housingId(), f.guestId(), f.status(), f.start(), f.end(), pageable);
    }

    /**
     * Find a booking by its id.
     */
    @Override
public BookingDetailDTO findBookingDetailById(Long id) {
    return repo.findBookingDetailById(id)
        .map(b -> new BookingDetailDTO(
            b.getId(),
            b.getCheckIn(),
            b.getCheckOut(),
            b.getGuestsNumber(),
            b.getStatus(),
            b.getTotalPrice(),
            b.getCreatedAt(),
            new HousingInfo(
                b.getHousing().getId(),
                b.getHousing().getTitle(),
                b.getHousing().getDescription(),
                b.getHousing().getAddress(),
                b.getHousing().getCity(),
                b.getHousing().getNightPrice(),
                b.getHousing().getMaxCapacity(),
                b.getHousing().getPrincipalImage(),
                b.getHousing().getAverageRating()
            ),
            new GuestInfo(
                b.getGuest().getId(),
                b.getGuest().getName(),
                b.getGuest().getEmail(),
                b.getGuest().getPhoneNumber()
            )
        )).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
}


    /**
     * Check if there are active or future bookings for a given housing. For housing service.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsFutureBookingsForHousing(Long housingId) {
        LocalDate today = LocalDate.now();
        return repo.existsActiveOrFutureBookingForHousing(housingId, List.of(BookingStatus.CONFIRMED), today);
    }
}