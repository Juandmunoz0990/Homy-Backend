package co.edu.uniquindio.application.Repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.edu.uniquindio.application.Models.Housing;


public interface HousingRepository extends JpaRepository<Housing, Long> {
    Page<Housing> findByCityContainingIgnoreCase(String ciudad, Pageable pageable);
    Page<Housing> findByMaxCapacityGreaterThanEqual(Integer capacity, Pageable pageable);
    Page<Housing> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("select h from Housing h where (city is null or h.city = :city) and  ")
    Page<Housing> findByTitleContainingIgnoreCase(String city, Pageable pageable);
    

}