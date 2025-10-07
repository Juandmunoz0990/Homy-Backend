package co.edu.uniquindio.application.Dtos.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.uniquindio.application.Models.enums.BookingStatus;
import lombok.Data;

@Data
public class BookingDetailDTO {

    private Long id;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestsNumber;
    private BookingStatus status;
    private Double totalPrice;
    private LocalDateTime createdAt;

    private HousingInfo housing;
    private GuestInfo guest;

    @Data
    public static class HousingInfo {
        private Long id;
        private String title;
        private String description;
        private String address;
        private String city;
        private Double nightPrice;
        private Integer maxCapacity;
        private String principalImage;
        private Double averageRating;

        public HousingInfo(Long id, String title, String description, String address,
                           String city, Double nightPrice, Integer maxCapacity,
                           String principalImage, Double averageRating) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.address = address;
            this.city = city;
            this.nightPrice = nightPrice;
            this.maxCapacity = maxCapacity;
            this.principalImage = principalImage;
            this.averageRating = averageRating;
        }

    }

    @Data
    public static class GuestInfo {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;

        public GuestInfo(Long id, String name, String email, String phoneNumber) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
        }

    }

    public BookingDetailDTO(Long id, LocalDate checkIn, LocalDate checkOut,
                            Integer guestsNumber, BookingStatus status, Double totalPrice,
                            LocalDateTime createdAt, HousingInfo housing, GuestInfo guest) {
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestsNumber = guestsNumber;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.housing = housing;
        this.guest = guest;
    }
}
