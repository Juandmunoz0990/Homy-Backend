package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Favorite;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.FavoriteRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.FavoriteService;
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.HousingMapper;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final HousingRepository housingRepository;
    private final UserService userService;
    private final HousingMapper housingMapper;

    @Override
    @Transactional
    public void addFavorite(Long guestId, Long housingId) {
        Housing housing = housingRepository.findById(housingId)
                .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class));

        if (Objects.equals(housing.getHostId(), guestId)) {
            throw new IllegalArgumentException("A host cannot mark their own housing as favorite");
        }

        if (Housing.STATE_DELETED.equals(housing.getState())) {
            throw new IllegalStateException("Housing is not available");
        }

        if (favoriteRepository.existsByGuestIdAndHousingId(guestId, housingId)) {
            return;
        }

        User guest = userService.findById(guestId);

        Favorite favorite = new Favorite();
        favorite.setGuest(guest);
        favorite.setHousing(housing);
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long guestId, Long housingId) {
        if (!favoriteRepository.existsByGuestIdAndHousingId(guestId, housingId)) {
            throw new ObjectNotFoundException("Favorite not found", Favorite.class);
        }
        favoriteRepository.deleteByGuestIdAndHousingId(guestId, housingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SummaryHousingResponse> getFavorites(Long guestId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Favorite> favorites = favoriteRepository.findActiveFavoritesByGuestId(guestId, pageable);

        return favorites.map(favorite -> housingMapper.toSummaryHousingResponse(favorite.getHousing()));
    }
}
