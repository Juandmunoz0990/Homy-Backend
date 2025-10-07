package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;

import co.edu.uniquindio.application.Models.enums.BookingStatus;
import lombok.Data;

@Data
public class BookingCreateDTO {

    private Long housingId;

    private Long guestId;

    private LocalDate checkIn;

    private LocalDate checkOut;

    private Integer guestsNumber;

    private BookingStatus state = BookingStatus.CONFIRMED;

    private Double totalPrice;
}
