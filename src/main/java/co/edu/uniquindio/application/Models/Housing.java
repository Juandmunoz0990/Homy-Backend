package co.edu.uniquindio.application.Models;

import java.util.ArrayList;
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
    @CollectionTable(name = "housing_services", joinColumns = @JoinColumn(name = "housing_id"))
    @Enumerated(EnumType.STRING)
    private List<ServicesEnum> services = new ArrayList<>();
    private String principalImage;
    @ElementCollection
    @CollectionTable(name = "housing_images", joinColumns = @JoinColumn(name = "housing_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> images = new ArrayList<>();
    private String state = STATE_ACTIVE;
    private Double averageRating = 0.0;
    @OneToMany(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookingsList;
    @OneToMany(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentsList;
    private Long hostId;
}