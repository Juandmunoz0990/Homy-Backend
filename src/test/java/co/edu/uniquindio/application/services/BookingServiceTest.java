package co.edu.uniquindio.application.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;
import co.edu.uniquindio.application.Dtos.booking.BookingDetailDTO;
import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.EmailService;

@SpringBootTest
@Transactional
public class BookingServiceTest {
    
    @Autowired
    private BookingService bookingService;

    // Desactivar para tests el envío real de emails
    @TestConfiguration
    static class NoopEmailConfig {
        @Bean
        public EmailService emailService() {
            return (emailDto) -> { /* no-op for tests */ };
        }
    }

    // -------------- Tests usando dataset.sql --------------

    /**
     * Test guardar booking
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testSaveBooking() {
        Long housingId = 1L;
        Long guestId = 2L;

        BookingCreateDTO dto = new BookingCreateDTO(
            housingId,
            guestId,
            LocalDate.now().plusDays(16),
            LocalDate.now().plusDays(18),
            2,
            300.0
        );

        assertDoesNotThrow(() -> {
            Booking b = bookingService.save(dto);
            assertNotNull(b.getId());
            assertEquals(housingId, b.getHousing().getId());
            assertEquals(guestId, b.getGuest().getId());
        });
    }

    /**
     * Test que espera que ocurra overlap si el dataset tiene una reserva ya confirmada
     * que choque con las fechas provistas.
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testSaveBooking_whenOverlap_thenThrows() {
        Long housingId = 1L;
        Long guestId = 2L;

        // Fechas que se solapan con una reserva del dataset
        LocalDate checkIn = LocalDate.of(2025, 10, 20);
        LocalDate checkOut = LocalDate.of(2025, 10, 25);

        BookingCreateDTO dto = new BookingCreateDTO(
            housingId, guestId, checkIn, checkOut, 2, 500.0
        );

        assertThrows(IllegalStateException.class, () -> bookingService.save(dto));
    }

    /**
     * Cancel booking successful
     * con checkIn suficientemente en el futuro (>= today + 2) y estado CONFIRMED.
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testCancelBooking_successful() {
        Long bookingId = 5L;
        Long guestId = 2L;

        assertDoesNotThrow(() -> bookingService.cancelBooking(bookingId, guestId));
    }

    /**
     * Cancel booking too close -> should throw.
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testCancelBooking_tooClose_thenThrows() {
        Long housingId = 1L;
        Long guestId = 2L;
        
        BookingCreateDTO dto = new BookingCreateDTO(
            housingId,
            guestId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            2,
            300.0
        );
        Booking b = bookingService.save(dto);

        assertThrows(IllegalStateException.class, () -> bookingService.cancelBooking(b.getId(), guestId));
    }

    /**
     * findBookingDetailById
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testFindBookingDetailById() {
        Long bookingId = 1L;

        BookingDetailDTO dto = bookingService.findBookingDetailById(bookingId);
        assertNotNull(dto);
        assertEquals(bookingId, dto.getId());
        assertNotNull(dto.getHousing());
        assertNotNull(dto.getGuest());
    }

    /**
     * existsFutureBookingsForHousing
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testExistsFutureAndConfirmedBookingsForHousing() {
        Long housingId = 1L;
        boolean result = bookingService.existsFutureBookingsForHousing(housingId);
        assertTrue(result);
    }
}