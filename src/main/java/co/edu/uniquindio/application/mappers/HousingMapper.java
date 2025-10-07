package co.edu.uniquindio.application.mappers;

import org.mapstruct.Mapper;

import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateHousingRequest;
import co.edu.uniquindio.application.Models.Housing;

@Mapper(componentModel = "spring")
public interface HousingMapper {
    
    Housing toHousing(CreateHousingRequest request);
}
