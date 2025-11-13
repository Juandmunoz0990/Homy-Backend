package co.edu.uniquindio.application.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByGuestIdAndHousingId(Long guestId, Long housingId);

    void deleteByGuestIdAndHousingId(Long guestId, Long housingId);

    List<Favorite> findByGuestId(Long guestId);

    @EntityGraph(attributePaths = {"housing"})
    @Query("""
        SELECT f
        FROM Favorite f
        JOIN f.housing h
        WHERE f.guest.id = :guestId
          AND h.state = 'active'
    """)
    Page<Favorite> findActiveFavoritesByGuestId(@Param("guestId") Long guestId, Pageable pageable);

    Long countByHousingId(Long housingId);
}
