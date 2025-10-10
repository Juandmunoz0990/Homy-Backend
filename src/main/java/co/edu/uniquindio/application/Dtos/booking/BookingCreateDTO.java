package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;

import co.edu.uniquindio.application.Models.enums.BookingStatus;

import jakarta.validation.constraints.*;

public record BookingCreateDTO(

    @NotNull(message = "El ID del alojamiento es obligatorio")
    Long housingId,

    @NotNull(message = "El ID del huésped es obligatorio")
    Long guestId,

    @NotNull(message = "La fecha de check-in es obligatoria")
    @FutureOrPresent(message = "La fecha de check-in debe ser hoy o una fecha futura")
    LocalDate checkIn,

    @NotNull(message = "La fecha de check-out es obligatoria")
    @Future(message = "La fecha de check-out debe ser una fecha futura")
    LocalDate checkOut,

    @NotNull(message = "El número de huéspedes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos un huésped")
    @Max(value = 10, message = "No se permiten más de 10 huéspedes por reserva")
    Integer guestsNumber,

    @NotNull(message = "El estado de la reserva es obligatorio")
    BookingStatus state,

    @NotNull(message = "El precio total es obligatorio")
    @Positive(message = "El precio total debe ser un valor positivo")
    Double totalPrice

) {}
