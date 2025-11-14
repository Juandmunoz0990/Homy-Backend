package co.edu.uniquindio.application.Controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.application.Dtos.User.ChangePasswordRequest;
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
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /**
     * Update basic user data
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, @Valid @RequestBody UserUpdateDTO dto) {
        Long id = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    /**
     * Update host info
     */
    @PutMapping("/{id}/host-info")
    public ResponseEntity<UserResponseDTO> updateHostInfo(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, @Valid @RequestBody HostDetailsUpdateDTO dto) {
        Long id = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        return ResponseEntity.ok(userService.updateHostInfo(id, dto));
    }

    /**
     * Send reset password code by email
     */
    @PostMapping("/forgot-password")
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

    /**
     * Verify reset code validity
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean valid = userService.validateResetCode(email, code);
        if (!valid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Código inválido o expirado"));

        return ResponseEntity.ok(Map.of("message", "Código válido, procede a restablecer tu contraseña"));
    }

    /**
     * Reset user password after verifying code
     */
    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        if (!userService.validateResetCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Código inválido o expirado"));
        }

        User user = userService.findByEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenRepository.deleteByEmail(request.getEmail());

        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida con éxito"));
    }
    
    /**
     * Change password by user's own decision (requires current password)
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long id = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        
        try {
            userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
