package co.edu.uniquindio.application.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Services.CommentService;

import java.util.List;

@RestController
@RequestMapping("/alojamientos/{alojamientoId}/comentarios")
public class CommentController {

    private final CommentService servicio;

    public CommentController(CommentService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> listar(@PathVariable Long alojamientoId) {
        return ResponseEntity.ok(servicio.findByHousingId(alojamientoId));
    }

    @PostMapping
    public ResponseEntity<Comment> crear(@RequestBody Comment c, @PathVariable Long alojamientoId) {
        c.setHousingId(alojamientoId);
        var creado = servicio.create(c);
        return ResponseEntity.status(201).body(creado);
    }
}