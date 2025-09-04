package co.edu.uniquindio.application.Services;

import java.util.List;
import java.util.Optional;

import co.edu.uniquindio.application.Models.Booking;

public interface BookingService {
    Booking create(Booking r);

    List<Booking> findByUsuarioId(Long usuarioId);

    List<Booking> findAll();

    Optional<Booking> findById(Long id);
}