package co.edu.uniquindio.application.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long housingId;
    private Long guestId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestsNumber;
    private BookingState estado;
    private Double totalPrecio;
    private Boolean pagado = false;
    private LocalDateTime creadoEn = LocalDateTime.now();

    private enum BookingState {
        PENDING, CONFIRMED, CANCELED, COMPLETED
    }
}