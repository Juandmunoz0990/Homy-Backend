package co.edu.uniquindio.application.Models;

import java.util.List;

import co.edu.uniquindio.application.Models.enums.ServicesEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "housings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Housing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String city;
    private String address;
    private Double latitude;
    private Double lenght;
    private Double nightPrice;
    private Integer maxCapacity;
    private List<ServicesEnum> services;
    private String principalImage;
    private String state = "active";
    private Double averageRating;
    @OneToMany
    private List<Booking> bookingsList;
    private Long hostId;


}