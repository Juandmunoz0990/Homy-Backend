package co.edu.uniquindio.application.Dtos.email;

public record EmailDTO(
        String subject,
        String body,
        String recipient
) {}