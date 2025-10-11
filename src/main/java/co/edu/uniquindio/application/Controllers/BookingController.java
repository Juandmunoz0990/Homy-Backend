package co.edu.uniquindio.application.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Dtos.ResponseDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingFilterDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingSummaryDTO;
import co.edu.uniquindio.application.Services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Save a new booking
     */
    @PostMapping
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<ResponseDTO<String>> save(@Valid @RequestBody BookingCreateDTO r) {
        bookingService.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>(true, "Booking created successfully"));
    }
    
    /** 
     * Cancel a booking by its id
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<Void> cancelBooking(@AuthenticationPrincipal User user, @PathVariable Long id) {
        Long guestId = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        bookingService.cancelBooking(id, guestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search bookings with filters. Hosts see bookings for their housings, guests see only their bookings.
     */
    @GetMapping("/search") //Example: GET /bookings/search?page=2
    @PreAuthorize("hasAnyAuthority('GUEST', 'HOST')")
    public ResponseEntity<ResponseDTO<Page<BookingSummaryDTO>>> searchBookings(
            @AuthenticationPrincipal User user,
            @ModelAttribute BookingFilterDTO filter,
            Pageable pageable) {
        
        Page<BookingSummaryDTO> bookings = bookingService.searchBookings(user, filter, pageable);
        return ResponseEntity.ok(new ResponseDTO<>(true, bookings));
    }

    /**
     * Get a booking by its id
     */
    @PreAuthorize("hasAnyAuthority('GUEST', 'HOST')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<BookingDetailDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>(true, bookingService.findBookingDetailById(id)));
    }
}