package co.edu.uniquindio.application.Dtos.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class HousingInfo {
    
    private Long id;
    private String title;
    private String description;
    private String address;
    private String city;
    private Double nightPrice;
    private Integer maxCapacity;
    private String principalImage;
    private Double averageRating;
}
