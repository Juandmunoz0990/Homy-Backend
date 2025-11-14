package co.edu.uniquindio.application.Dtos.Housing.Responses;

public record SummaryHousingResponse (
    Long id,
    String title,
    String city,
    Double nightPrice,
    String principalImage,
    Double averageRating
){
    
}
