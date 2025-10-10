package co.edu.uniquindio.application.Services;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;

@Service
public interface HousingService {

    EntityCreatedResponse create(Long hostId, CreateOrEditHousingRequest request);
    EntityChangedResponse delete(Long housingId, Long hostId);
    EntityChangedResponse edit(Long housingId, Long hostId, CreateOrEditHousingRequest request);
    Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut, Integer totalGuests, Integer minPrice, Integer maxPrice);
    Housing findById(Long id);
    Boolean existsHousing(Long housingId, Long hostId);
    

    
}