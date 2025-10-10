package co.edu.uniquindio.application.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Dtos.ResponseDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingFilterDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingSummaryDTO;
import co.edu.uniquindio.application.Security.CustomUserDetails;
import co.edu.uniquindio.application.Services.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Save a new booking
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<String>> save(@Valid @RequestBody BookingCreateDTO r) {
        bookingService.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>(true, "Booking created successfully"));
    }
    
    /**
     * Cancel a booking by its id
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) {
        Long guestId = user.getId();
        bookingService.cancelBooking(id, guestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search bookings with filters. Hosts see bookings for their housings, guests see only their bookings.
     */
    @GetMapping("/search") //Example: GET /bookings/search?page=2
    public ResponseEntity<Page<BookingSummaryDTO>> searchBookings(
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute BookingFilterDTO filter,
            Pageable pageable) {
        
        Page<BookingSummaryDTO> bookings = bookingService.searchBookings(user, filter, pageable);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get a booking by its id
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findBookingDetailById(id));
    }
}