package co.edu.uniquindio.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;

@Mapper(componentModel = "spring")
public interface HousingMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hostId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "bookingsList", ignore = true)
    @Mapping(target = "commentsList", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(source = "pricePerNight", target = "nightPrice")
    @Mapping(target = "principalImage", ignore = true)
    Housing toHousing(CreateOrEditHousingRequest request);

    @Mapping(target = "hostName", ignore = true)
    HousingResponse toHousingResponse (Housing housing);

    SummaryHousingResponse toSummaryHousingResponse (Housing housing);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hostId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "bookingsList", ignore = true)
    @Mapping(target = "commentsList", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(source = "pricePerNight", target = "nightPrice")
    @Mapping(target = "principalImage", ignore = true)
    void updateHousingFromRequest(CreateOrEditHousingRequest request, @MappingTarget Housing housing);
}
