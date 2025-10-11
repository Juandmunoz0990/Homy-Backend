package co.edu.uniquindio.application.Dtos.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest { //Para setear la nueva contraseña luego de validar el código
    private String email;
    private String code;
    private String newPassword;
}