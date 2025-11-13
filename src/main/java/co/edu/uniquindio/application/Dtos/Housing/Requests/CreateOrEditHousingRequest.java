package co.edu.uniquindio.application.Dtos.Housing.Requests;

import java.util.List;

import co.edu.uniquindio.application.Models.enums.ServicesEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrEditHousingRequest (
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String city,
    @NotNull Double latitude,
    @NotNull Double length,
    @NotBlank String address,
    @NotNull @Positive Integer maxCapacity,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) Double pricePerNight,
    @NotEmpty List<ServicesEnum> services,
    @NotNull @Size(min = 1, max = 10) List<@NotBlank String> imagesUrls
){

}
