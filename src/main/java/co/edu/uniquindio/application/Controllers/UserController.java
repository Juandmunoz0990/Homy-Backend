package co.edu.uniquindio.application.Controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.application.Dtos.User.HostDetailsUpdateDTO;
import co.edu.uniquindio.application.Dtos.User.PasswordResetRequest;
import co.edu.uniquindio.application.Dtos.User.UserResponseDTO;
import co.edu.uniquindio.application.Dtos.User.UserUpdateDTO;
import co.edu.uniquindio.application.Dtos.email.EmailDTO;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.PasswordResetTokenRepository;
import co.edu.uniquindio.application.Repositories.UserRepository;
import co.edu.uniquindio.application.Services.EmailService;
import co.edu.uniquindio.application.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @Valid @RequestBody UserUpdateDTO dto) {
        Long id = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PutMapping("/{id}/host-info")
    public ResponseEntity<UserResponseDTO> updateHostInfo(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @Valid @RequestBody HostDetailsUpdateDTO dto) {
        Long id = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        return ResponseEntity.ok(userService.updateHostInfo(id, dto));
    }

    @PostMapping("/forgot-password") // Enviar email para restablecer contraseña
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> email) throws Exception {

        String emailValue = email.get("email");
        User user = userService.findByEmail(emailValue);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Correo no registrado"));
        }

        String code = userService.generateResetCode(emailValue);

        emailService.sendMail(new EmailDTO("Reset code", code, emailValue));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "Código de verificación enviado al correo."));
    }

    @PostMapping("/verify-code") // Verificar si el código es correcto
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean valid = userService.validateResetCode(email, code);
        if (!valid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido o expirado");

        return ResponseEntity.ok("Código válido, procede a restablecer tu contraseña.");
    }

    @Transactional // Por el delete del token
    @PostMapping("/reset-password") // Para cambiar la contraseña
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        if (!userService.validateResetCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido o expirado");
        } // Verifica de nuevo el código

        User user = userService.findByEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user); // Sobreescribe el User con la nueva contraseña

        tokenRepository.deleteByEmail(request.getEmail());

        return ResponseEntity.ok("Contraseña restablecida con éxito.");
    }
}