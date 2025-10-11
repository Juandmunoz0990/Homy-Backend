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

    public static final String STATE_ACTIVE = "active";
    public static final String STATE_DELETED = "deleted";
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String city;
    private String address;
    private Double latitude;
    private Double length;
    private Double nightPrice;
    private Integer maxCapacity;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<ServicesEnum> services;
    private String principalImage;
    @Column(columnDefinition = "TEXT")
    private List<String> images;
    private String state = STATE_ACTIVE;
    private Double averageRating;
    @OneToMany(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookingsList;
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentsList;
    private Long hostId;
}