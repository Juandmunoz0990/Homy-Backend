package co.edu.uniquindio.mappers;

import org.mapstruct.Mapper;

import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;

@Mapper(componentModel = "spring")
public interface HousingMapper {
    
    Housing toHousing(CreateOrEditHousingRequest request);
    SummaryHousingResponse toSummaryHousingResponse (Housing housing);
}
