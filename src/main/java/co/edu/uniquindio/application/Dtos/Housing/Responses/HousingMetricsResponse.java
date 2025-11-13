package co.edu.uniquindio.application.Dtos.Housing.Responses;

public record HousingMetricsResponse(
        Long housingId,
        Long totalBookings,
        Double averageRating,
        Long favoritesCount
) {}
