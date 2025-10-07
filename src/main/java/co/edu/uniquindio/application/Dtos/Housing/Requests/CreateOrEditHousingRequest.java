package co.edu.uniquindio.application.Dtos.Housing.Requests;

import java.util.List;

import co.edu.uniquindio.application.Models.enums.ServicesEnum;

public record CreateOrEditHousingRequest (
    String title,
    String description,
    String city,
    Double latitude,
    Double length,
    String address,
    Integer maxCapacity,
    Double pricePerNight,
    List<ServicesEnum> services,
    List<String> imagesUrls
){
    
}
