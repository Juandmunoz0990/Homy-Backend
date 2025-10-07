package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;

import co.edu.uniquindio.application.Models.enums.BookingStatus;
import lombok.Data;

@Data
public class BookingFilterDTO {
    
    private Long housingId; //Only for host
    private Long guestId; //Only for guest
    private BookingStatus status;
    private LocalDate start;
    private LocalDate end;
}
