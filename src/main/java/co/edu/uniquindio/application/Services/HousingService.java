package co.edu.uniquindio.application.Services;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;

public interface HousingService {
    EntityCreatedResponse create(CreateHousingRequest request);
    Page<SummaryHousingResponse> searchHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut, Integer totalGuests, Integer indexPage);
    
    Housing findById(Long id);

    void deleteById(Long id);
}