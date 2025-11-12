package co.edu.uniquindio.application.Dtos.Host;

import java.util.List;

public record HostDashboardMetricsDTO(
    Integer totalProperties,
    Integer totalBookings,
    Integer upcomingBookings,
    Integer completedBookings,
    Double totalRevenue,
    Double averageRating,
    List<PropertyMetricDTO> propertyMetrics
) {}
