package co.edu.uniquindio.application.Services.impl;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.HousingService;

import java.util.List;
import java.util.Optional;

@Service
public class HousingServiceImpl implements HousingService {

    private final HousingRepository repo;

    public HousingServiceImpl(HousingRepository repo) {
        this.repo = repo;
    }

    @Override
    public Housing create(Housing a) {
        return repo.save(a);
    }

    @Override
    public List<Housing> searchByCiudad(String ciudad) {
        return repo.findByCityContainingIgnoreCase(ciudad == null ? "" : ciudad);
    }

    @Override
    public Optional<Housing> findById(Long id) {
        return repo.findById(id);
    }
}