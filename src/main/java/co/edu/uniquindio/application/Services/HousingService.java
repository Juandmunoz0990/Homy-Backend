package co.edu.uniquindio.application.Services;

import java.util.List;
import java.util.Optional;

import co.edu.uniquindio.application.Models.Housing;

public interface HousingService {
    Housing create(Housing a);

    List<Housing> searchByCiudad(String ciudad);

    Optional<Housing> findById(Long id);
}