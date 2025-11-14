package co.edu.uniquindio.application.Repositories;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.Housing;

@Repository
public interface HousingRepository extends JpaRepository<Housing, Long> {

  @Query("""
    SELECT DISTINCT h
    FROM Housing h
    WHERE (:city IS NULL OR LOWER(h.city) = LOWER(:city))
      AND (:minPrice IS NULL OR h.nightPrice >= :minPrice)
      AND (:maxPrice IS NULL OR h.nightPrice <= :maxPrice)
      AND h.state = 'active'
      AND NOT EXISTS (
          SELECT b FROM Booking b
          WHERE b.housing = h
            AND b.status = 'CONFIRMED'
            AND b.checkIn < :checkOut
            AND b.checkOut > :checkIn
      )
    """)
Page<Housing> findHousingsByFilters(
        @Param("city") String city,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
);

  Boolean existsByIdAndHostId(Long housingId, Long hostId);

  @Query(value = "SELECT h.id, h.title, h.city, h.night_price, h.principal_image, h.average_rating FROM housings h WHERE h.host_id = :hostId AND (h.state IS NULL OR h.state = '' OR h.state = 'active') ORDER BY h.id DESC", nativeQuery = true, countQuery = "SELECT COUNT(*) FROM housings h WHERE h.host_id = :hostId AND (h.state IS NULL OR h.state = '' OR h.state = 'active')")
  Page<Object[]> findByHostIdNative(@Param("hostId") Long hostId, Pageable pageable);
  
  @Query("SELECT h FROM Housing h WHERE h.hostId = :hostId AND (h.state IS NULL OR h.state = '' OR h.state = 'active') ORDER BY h.id DESC")
  Page<Housing> findByHostId(@Param("hostId") Long hostId, Pageable pageable);

  @Modifying
  @Query("UPDATE Housing h SET h.state = 'deleted' WHERE h.id = :housingId AND h.hostId = :hostId")
  void softDeleteByIdAndHostId(@Param("housingId") Long housingId, @Param("hostId") Long hostId);
  
  // Query específica para obtener housing sin cargar relaciones lazy
  @Query("SELECT h FROM Housing h WHERE h.id = :housingId")
  java.util.Optional<Housing> findByIdWithoutRelations(@Param("housingId") Long housingId);
  
  // Query nativa para obtener todos los campos sin problemas con ElementCollection
  // Incluir estado para verificar que no esté eliminado
  @Query(value = """
    SELECT h.id, h.title, h.description, h.city, h.address, h.latitude, h.length, 
           h.night_price, h.max_capacity, h.principal_image, h.state, h.average_rating, h.host_id
    FROM housings h 
    WHERE h.id = :housingId AND (h.state IS NULL OR h.state = '' OR h.state = 'active')
    """, nativeQuery = true)
  java.util.Optional<Object[]> findByIdNative(@Param("housingId") Long housingId);
  
  // Query para obtener servicios de un housing (JPA crea tabla automáticamente con nombre: Housing_services)
  @Query(value = "SELECT services FROM Housing_services WHERE Housing_id = :housingId", nativeQuery = true)
  List<String> findServicesByHousingId(@Param("housingId") Long housingId);
  
  // Query para obtener imágenes de un housing (JPA crea tabla automáticamente con nombre: Housing_images)
  @Query(value = "SELECT images FROM Housing_images WHERE Housing_id = :housingId ORDER BY Housing_id", nativeQuery = true)
  List<String> findImagesByHousingId(@Param("housingId") Long housingId);
}
