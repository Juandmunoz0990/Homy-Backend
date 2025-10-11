package co.edu.uniquindio.application.controllers;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.uniquindio.application.Dtos.User.HostDetailsUpdateDTO;
import co.edu.uniquindio.application.Dtos.User.PasswordResetRequest;
import co.edu.uniquindio.application.Dtos.User.UserUpdateDTO;
import co.edu.uniquindio.application.Models.PasswordResetToken;
import co.edu.uniquindio.application.Repositories.PasswordResetTokenRepository;
import co.edu.uniquindio.application.Services.EmailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    // Desactivar para tests el envío real de emails
    @TestConfiguration
    static class NoopEmailConfig {
        @Bean
        public EmailService emailService() {
            return (emailDto) -> { /* no-op for tests */ };
        }
    }

    // ---------- UPDATE USER ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "1", authorities = {"HOST"})
    void testUpdateUserSuccess() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Carlos Actualizado",
                "3009999999",
                "nueva-imagen.jpg"
        );

        mockMvc.perform(put("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carlos Actualizado"))
                .andExpect(jsonPath("$.phoneNumber").value("3009999999"));
    }

    @Test
    @Sql("classpath:dataset.sql")
    void testUpdateUserUnauthorized() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Carlos Actualizado",
                "3009999999",
                "nueva-imagen.jpg"
        );

        mockMvc.perform(put("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- UPDATE HOST INFO ----------

    @Test
    @Sql("classpath:dataset.sql")
    @WithMockUser(username = "1", authorities = {"HOST"})
    void testUpdateHostInfoSuccess() throws Exception {
        HostDetailsUpdateDTO dto = new HostDetailsUpdateDTO(
                "Descripción del host",
                "documentos-legales.pdf"
        );

        mockMvc.perform(put("/users/1/host-info")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @Sql("classpath:dataset.sql")
    void testUpdateHostInfoUnauthorized() throws Exception {
        HostDetailsUpdateDTO dto = new HostDetailsUpdateDTO(
                "Descripción del host",
                "documentos-legales.pdf"
        );

        mockMvc.perform(put("/users/1/host-info")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- FORGOT PASSWORD ----------

    @Test
    @Sql("classpath:dataset.sql")
    void testForgotPasswordSuccess() throws Exception {
        Map<String, String> request = Map.of("email", "carlos@gmail.com");

        mockMvc.perform(post("/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código de verificación enviado al correo."));
    }

    @Test
    @Sql("classpath:dataset.sql")
    void testForgotPasswordEmailNotFound() throws Exception {
        Map<String, String> request = Map.of("email", "noexiste@gmail.com");

        mockMvc.perform(post("/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Correo no registrado"));
    }

    // ---------- VERIFY CODE ----------

    @Test
    @Sql("classpath:dataset.sql")
    void testVerifyCodeSuccess() throws Exception {
        String email = "carlos@gmail.com";
        Map<String, String> forgotRequest = Map.of("email", email);

        // Generar código
        mockMvc.perform(post("/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(forgotRequest)));

        // Obtener código de la BD
        PasswordResetToken token = tokenRepository.findByEmail(email).get();
        String code = token.getCode();

        Map<String, String> verifyRequest = Map.of("email", email, "code", code);

        mockMvc.perform(post("/verify-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Código válido, procede a restablecer tu contraseña."));
    }

    @Test
    @Sql("classpath:dataset.sql")
    void testVerifyCodeInvalid() throws Exception {
        String email = "carlos@gmail.com";
        Map<String, String> forgotRequest = Map.of("email", email);

        // Generar código
        mockMvc.perform(post("/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(forgotRequest)));

        Map<String, String> verifyRequest = Map.of("email", email, "code", "999999");

        mockMvc.perform(post("/verify-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Código inválido o expirado"));
    }

    // ---------- RESET PASSWORD ----------

    @Test
    @Sql("classpath:dataset.sql")
    void testResetPasswordSuccess() throws Exception {
        String email = "carlos@gmail.com";
        String newPassword = "newpass123";
        Map<String, String> forgotRequest = Map.of("email", email);

        // Generar código
        mockMvc.perform(post("/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(forgotRequest)));

        // Obtener código
        PasswordResetToken token = tokenRepository.findByEmail(email).get();
        String code = token.getCode();

        PasswordResetRequest resetRequest = new PasswordResetRequest(email, code, newPassword);

        mockMvc.perform(post("/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña restablecida con éxito."));
    }

    @Test
    @Sql("classpath:dataset.sql")
    void testResetPasswordInvalidCode() throws Exception {
        String email = "carlos@gmail.com";
        String newPassword = "newpass123";
        Map<String, String> forgotRequest = Map.of("email", email);

        // Generar código
        mockMvc.perform(post("/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(forgotRequest)));

        PasswordResetRequest resetRequest = new PasswordResetRequest(email, "999999", newPassword);

        mockMvc.perform(post("/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Código inválido o expirado"));
    }
}