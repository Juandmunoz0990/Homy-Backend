package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.application.Models.Housing;

import java.util.List;

public interface HousingRepository extends JpaRepository<Housing, Long> {
    List<Housing> findByCityContainingIgnoreCase(String ciudad);
}