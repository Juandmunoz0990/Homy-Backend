package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;

import co.edu.uniquindio.application.Models.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryDTO { //For listing bookings with essential info
    
    private Long id;
    private String housingTitle;
    private String principalImage;
    private String city;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestsNumber;
    private BookingStatus status;
    private Double totalPrice;
    private String guestName; // útil si es una vista del host
}
