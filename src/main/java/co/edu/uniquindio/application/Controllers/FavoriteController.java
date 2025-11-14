package co.edu.uniquindio.application.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Security.CustomUserDetails;
import co.edu.uniquindio.application.Services.FavoriteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{housingId}")
    public ResponseEntity<EntityCreatedResponse> addFavorite(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long housingId) {
        Long userId = user.getId();
        EntityCreatedResponse response = favoriteService.addFavorite(userId, housingId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{housingId}")
    public ResponseEntity<EntityChangedResponse> removeFavorite(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long housingId) {
        Long userId = user.getId();
        EntityChangedResponse response = favoriteService.removeFavorite(userId, housingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Long>>> getUserFavorites(
            @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        List<Long> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(Map.of("housingIds", favorites));
    }

    @GetMapping("/{housingId}/count")
    public ResponseEntity<Map<String, Long>> getFavoriteCount(@PathVariable Long housingId) {
        Long count = favoriteService.getFavoriteCount(housingId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/{housingId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long housingId) {
        Long userId = user.getId();
        boolean isFavorite = favoriteService.isFavorite(userId, housingId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }
}

