package co.edu.uniquindio.application.mappers;

import org.mapstruct.Mapper;

import co.edu.uniquindio.application.Dtos.auth.RegisterRequest;
import co.edu.uniquindio.application.Models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    User toUser(RegisterRequest dto);
}
