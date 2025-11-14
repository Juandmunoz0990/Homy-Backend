package co.edu.uniquindio.application.Services.impl;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Models.Favorite;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.FavoriteRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Repositories.UserRepository;
import co.edu.uniquindio.application.Services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final HousingRepository housingRepository;

    @Override
    public EntityCreatedResponse addFavorite(Long userId, Long housingId) {
        // Verificar si ya existe
        if (favoriteRepository.existsByUserIdAndHousingId(userId, housingId)) {
            throw new IllegalStateException("Este alojamiento ya está en tus favoritos");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Usuario no encontrado", User.class));
        
        Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Alojamiento no encontrado", Housing.class));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setHousing(housing);
        favoriteRepository.save(favorite);

        return new EntityCreatedResponse("Alojamiento agregado a favoritos", Instant.now());
    }

    @Override
    public EntityChangedResponse removeFavorite(Long userId, Long housingId) {
        Favorite favorite = favoriteRepository.findByUserIdAndHousingId(userId, housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Favorito no encontrado", Favorite.class));

        favoriteRepository.delete(favorite);
        return new EntityChangedResponse("Alojamiento eliminado de favoritos", Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId)
            .stream()
            .map(f -> f.getHousing().getId())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFavoriteCount(Long housingId) {
        return favoriteRepository.countByHousingId(housingId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long housingId) {
        return favoriteRepository.existsByUserIdAndHousingId(userId, housingId);
    }
}

