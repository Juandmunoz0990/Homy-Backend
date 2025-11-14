package co.edu.uniquindio.application.Dtos.Housing.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCalendarResponse {
    private Long housingId;
    private String housingTitle;
    private List<LocalDate> bookedDates;
    private List<LocalDate> availableDates;
}

