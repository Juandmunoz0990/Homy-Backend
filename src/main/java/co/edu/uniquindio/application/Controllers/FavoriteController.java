package co.edu.uniquindio.application.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Security.CustomUserDetails;
import co.edu.uniquindio.application.Services.FavoriteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{housingId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long housingId,
                                            @AuthenticationPrincipal CustomUserDetails user) {
        favoriteService.addFavorite(user.getId(), housingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{housingId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long housingId,
                                               @AuthenticationPrincipal CustomUserDetails user) {
        favoriteService.removeFavorite(user.getId(), housingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<SummaryHousingResponse>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SummaryHousingResponse> favorites = favoriteService.getFavorites(user.getId(), page, size);
        return ResponseEntity.ok(favorites);
    }
}
