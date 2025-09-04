package co.edu.uniquindio.application.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Services.HousingService;

import java.util.List;

@RestController
@RequestMapping("/housings")
public class HousingController {

    private final HousingService servicio;

    public HousingController(HousingService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<Housing>> listar(@RequestParam(required = false) String ciudad) {
        return ResponseEntity.ok(servicio.searchByCiudad(ciudad));
    }

    @PostMapping
    public ResponseEntity<Housing> crear(@RequestBody Housing a) {
        var creado = servicio.create(a);
        return ResponseEntity.status(201).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Housing> detalle(@PathVariable Long id) {
        return servicio.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}