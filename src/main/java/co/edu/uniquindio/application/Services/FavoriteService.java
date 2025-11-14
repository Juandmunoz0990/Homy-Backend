package co.edu.uniquindio.application.Services;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;

import java.util.List;

public interface FavoriteService {
    EntityCreatedResponse addFavorite(Long userId, Long housingId);
    EntityChangedResponse removeFavorite(Long userId, Long housingId);
    List<Long> getUserFavorites(Long userId);
    Long getFavoriteCount(Long housingId);
    boolean isFavorite(Long userId, Long housingId);
}

