package co.edu.uniquindio.application.Services.impl;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Services.BookingService;

import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repo;

    public BookingServiceImpl(BookingRepository repo) {
        this.repo = repo;
    }

    @Override
    public Booking create(Booking r) {
        return repo.save(r);
    }

    @Override
    public List<Booking> findByUsuarioId(Long usuarioId) {
        return repo.findByGuestId(usuarioId);
    }

    @Override
    public List<Booking> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return repo.findById(id);
    }
}