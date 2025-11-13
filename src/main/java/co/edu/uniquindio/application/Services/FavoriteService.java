package co.edu.uniquindio.application.Services;

import org.springframework.data.domain.Page;

import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;

public interface FavoriteService {

    void addFavorite(Long guestId, Long housingId);

    void removeFavorite(Long guestId, Long housingId);

    Page<SummaryHousingResponse> getFavorites(Long guestId, int page, int size);
}
