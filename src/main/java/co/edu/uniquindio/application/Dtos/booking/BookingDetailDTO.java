package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.uniquindio.application.Models.enums.BookingStatus;
import lombok.Data;

@Data
public class BookingDetailDTO {

    private Long id;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestsNumber;
    private BookingStatus status;
    private Double totalPrice;
    private LocalDateTime createdAt;

    private HousingInfo housing;
    private GuestInfo guest;

    public BookingDetailDTO(Long id, LocalDate checkIn, LocalDate checkOut,
                            Integer guestsNumber, BookingStatus status, Double totalPrice,
                            LocalDateTime createdAt, HousingInfo housing, GuestInfo guest) {
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestsNumber = guestsNumber;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.housing = housing;
        this.guest = guest;
    }
}
