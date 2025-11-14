package co.edu.uniquindio.application.Services;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.AvailabilityCalendarResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;

public interface HousingService {

    EntityCreatedResponse create(Long hostId, CreateOrEditHousingRequest request);
    EntityChangedResponse delete(Long housingId, Long hostId);
    EntityChangedResponse edit(Long housingId, Long hostId, CreateOrEditHousingRequest request);
    Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut, Double minPrice, Double maxPrice, Integer totalGuests);
    Page<SummaryHousingResponse> getHousingsByHost(Long hostId, Integer page, Integer size);
    HousingResponse getHousingDetail(Long housingId);
    Housing findById(Long id);
    Boolean existsHousing(Long housingId, Long hostId);
    HousingMetricsResponse getHousingMetrics(Long housingId, Long hostId, LocalDate dateFrom, LocalDate dateTo);
    AvailabilityCalendarResponse getAvailabilityCalendar(Long housingId, LocalDate startDate, LocalDate endDate);
}