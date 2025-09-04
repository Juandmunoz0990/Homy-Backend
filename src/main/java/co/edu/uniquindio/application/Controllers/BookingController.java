package co.edu.uniquindio.application.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Services.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService servicio;

    public BookingController(BookingService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Booking> crear(@RequestBody Booking r) {
        var creado = servicio.create(r);
        return ResponseEntity.status(201).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> listar(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId != null)
            return ResponseEntity.ok(servicio.findByUsuarioId(usuarioId));
        return ResponseEntity.ok(servicio.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@PathVariable Long id) {
        return servicio.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}