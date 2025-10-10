package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;

import co.edu.uniquindio.application.Models.enums.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;

public record BookingFilterDTO(

    Long housingId,  // Solo para anfitrión
    Long guestId,    // Solo para huésped
    BookingStatus status,

    @PastOrPresent(message = "La fecha de inicio no puede ser futura")
    LocalDate start,

    @FutureOrPresent(message = "La fecha de fin debe ser hoy o una fecha futura")
    LocalDate end

) {}