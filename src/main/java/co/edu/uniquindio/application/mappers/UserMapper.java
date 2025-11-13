package co.edu.uniquindio.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uniquindio.application.Dtos.auth.RegisterRequest;
import co.edu.uniquindio.application.Models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    User toUser(RegisterRequest dto);
}
