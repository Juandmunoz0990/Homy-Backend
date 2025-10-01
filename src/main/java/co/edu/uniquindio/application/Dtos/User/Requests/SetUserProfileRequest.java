package co.edu.uniquindio.application.Dtos.User.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SetUserProfileRequest(
    @NotBlank(message = "The name cannot be neither empty nor null")
    String name,

    @Email
    String email,

    @NotBlank(message = "The phone number cannot be neither empty nor null")
    String phoneNumber
) {

}
