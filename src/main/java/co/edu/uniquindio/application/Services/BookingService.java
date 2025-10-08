package co.edu.uniquindio.application.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingFilterDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingSummaryDTO;
import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Security.CustomUserDetails;

public interface BookingService {

    Booking save(BookingCreateDTO b);

    Page<BookingSummaryDTO> searchBookings(CustomUserDetails user, BookingFilterDTO filter, Pageable pageable);

    void cancelBooking(Long id, Long guestId);

    BookingDetailDTO findBookingDetailById(Long id);

    boolean existsFutureBookingsForHousing(Long housingId);
}