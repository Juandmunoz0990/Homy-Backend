package co.edu.uniquindio.application.Dtos.Housing.Responses;

import java.util.List;

import co.edu.uniquindio.application.Models.Booking;
import co.edu.uniquindio.application.Models.enums.ServicesEnum;
import lombok.Data;

@Data
public class HousingResponse {
    private String title;
    private String description;
    private String city;
    private String address;
    private Double latitude;
    private Double length;
    private Double nightPrice;
    private Integer maxCapacity;
    private List<ServicesEnum> services;
    private List<String> images;
    private Double averageRating;
    private List<Booking> bookingsList;
    private String hostName;
}

