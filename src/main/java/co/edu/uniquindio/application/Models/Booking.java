package co.edu.uniquindio.application.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.uniquindio.application.Models.enums.BookingStatus;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Housing housing;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User guest;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer guestsNumber;

    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private Double totalPrice;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
