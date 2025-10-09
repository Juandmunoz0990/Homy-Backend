package co.edu.uniquindio.application.mappers;

import org.mapstruct.*;

import co.edu.uniquindio.application.Dtos.booking.BookingCreateDTO;
import co.edu.uniquindio.application.Models.Booking;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {
    

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "guest", ignore = true)
    @Mapping(target = "housing", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "CONFIRMED")
    Booking toBooking(BookingCreateDTO dto);
}