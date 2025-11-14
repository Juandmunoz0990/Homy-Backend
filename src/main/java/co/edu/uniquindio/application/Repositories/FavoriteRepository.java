package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.application.Models.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndHousingId(Long userId, Long housingId);
    
    List<Favorite> findByUserId(Long userId);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.housing.id = :housingId")
    Long countByHousingId(@Param("housingId") Long housingId);
    
    boolean existsByUserIdAndHousingId(Long userId, Long housingId);
}

