package co.edu.uniquindio.application.Dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password

) {}