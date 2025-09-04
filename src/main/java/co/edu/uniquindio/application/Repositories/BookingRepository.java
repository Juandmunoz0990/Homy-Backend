package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.application.Models.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestId(Long usuarioId);

    List<Booking> findByHousingId(Long alojamientoId);
}