package co.edu.uniquindio.application.Dtos.Host;

public record PropertyMetricDTO(
    Integer propertyId,
    String title,
    Integer totalBookings,
    Double revenue,
    Double averageRating
) {}
