package co.edu.uniquindio.application.Dtos.auth;

import java.util.Date;

import co.edu.uniquindio.application.Models.User.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private Date birthDate;
    private Role role;
}