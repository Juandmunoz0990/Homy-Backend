package co.edu.uniquindio.application.Models;

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
    private ServicesEnum services;
    private String imagenPrincipal;
    private String estado = "activo";
    private Long anfitrionId;

    private enum ServicesEnum {
        WIFI, PARKING, POOL, GYM, PETS_ALLOWED, AIR_CONDITIONING, BREAKFAST_INCLUDED
    }
}