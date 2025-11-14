package co.edu.uniquindio.application.Dtos.Housing.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HousingMetricsResponse {
    private Long housingId;
    private String housingTitle;
    private Long totalBookings;
    private Double averageRating;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}

