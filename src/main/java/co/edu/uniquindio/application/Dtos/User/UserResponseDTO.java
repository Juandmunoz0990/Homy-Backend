package co.edu.uniquindio.application.Dtos.User;

import co.edu.uniquindio.application.Models.enums.Role;

public record UserResponseDTO(
    Long id,
    String name,
    String email,
    String phoneNumber,
    String profileImageUrl,
    Role role
) {}
