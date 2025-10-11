package co.edu.uniquindio.application.Dtos.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HostDetailsUpdateDTO(

    @NotBlank(message = "La descripción personal es obligatoria")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    String description,

    String legalDocumentsUrl

) {}