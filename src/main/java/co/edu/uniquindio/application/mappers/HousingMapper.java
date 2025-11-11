package co.edu.uniquindio.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;

@Mapper(componentModel = "spring")
public interface HousingMapper {
    
    @Mapping(source = "pricePerNight", target = "nightPrice")
    Housing toHousing(CreateOrEditHousingRequest request);
    
    SummaryHousingResponse toSummaryHousingResponse (Housing housing);
    HousingResponse toHousingResponse (Housing housing);
}
