package co.edu.uniquindio.application.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.application.Dtos.User.HostDetailsUpdateDTO;
import co.edu.uniquindio.application.Dtos.User.UserResponseDTO;
import co.edu.uniquindio.application.Dtos.User.UserUpdateDTO;
import co.edu.uniquindio.application.Dtos.auth.RegisterRequest;
import co.edu.uniquindio.application.Models.HostDetails;
import co.edu.uniquindio.application.Models.PasswordResetToken;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.HostDetailsRepository;
import co.edu.uniquindio.application.Repositories.PasswordResetTokenRepository;
import co.edu.uniquindio.application.Repositories.UserRepository;
import co.edu.uniquindio.application.Services.EmailService;
import co.edu.uniquindio.application.Services.UserService;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HostDetailsRepository hostDetailsRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Desactivar para tests el envío real de emails
    @TestConfiguration
    static class NoopEmailConfig {
        @Bean
        public EmailService emailService() {
            return (emailDto) -> { /* no-op for tests */ };
        }
    }

    /**
     * Test registrar usuario exitosamente
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testRegister_successful() {
        RegisterRequest request = new RegisterRequest(
            "Nuevo Usuario",
            "nuevo@gmail.com",
            "Password123",
            "1234567890",
            new Date(),
            null
        );

        assertDoesNotThrow(() -> {
            User user = userService.register(request);
            assertNotNull(user.getId());
            assertEquals("nuevo@gmail.com", user.getEmail());
            assertTrue(passwordEncoder.matches("Password123", user.getPassword()));
        });
    }

    /**
     * Test registrar con email duplicado lanza excepción
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testRegister_duplicateEmail_thenThrows() {
        RegisterRequest request = new RegisterRequest(
            "Carlos Gómez",
            "carlos@gmail.com",  // Email existente
            "Pass1234",
            "3001234567",
            new Date(),
            null
        );

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    /**
     * Test encontrar usuario por email
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testFindByEmail_successful() {
        String email = "carlos@gmail.com";

        assertDoesNotThrow(() -> {
            User user = userService.findByEmail(email);
            assertNotNull(user);
            assertEquals(1L, user.getId());
        });
    }

    /**
     * Test encontrar usuario por email no encontrado lanza excepción
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testFindByEmail_notFound_thenThrows() {
        String email = "noexiste@gmail.com";

        assertThrows(ObjectNotFoundException.class, () -> userService.findByEmail(email));
    }

    /**
     * Test encontrar usuario por ID
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testFindById_successful() {
        Long id = 1L;

        assertDoesNotThrow(() -> {
            User user = userService.findById(id);
            assertNotNull(user);
            assertEquals("carlos@gmail.com", user.getEmail());
        });
    }

    /**
     * Test encontrar usuario por ID no encontrado lanza excepción
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testFindById_notFound_thenThrows() {
        Long id = 999L;

        assertThrows(ObjectNotFoundException.class, () -> userService.findById(id));
    }

    /**
     * Test actualizar usuario
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testUpdateUser_successful() {
        Long userId = 1L;
        UserUpdateDTO dto = new UserUpdateDTO(
            "Carlos Actualizado",
            "3009999999",
            "nueva-imagen.jpg"
        );

        assertDoesNotThrow(() -> {
            UserResponseDTO response = userService.updateUser(userId, dto);
            assertNotNull(response);
            assertEquals("Carlos Actualizado", response.name());
            assertEquals("3009999999", response.phoneNumber());

            // Verificar en BD
            User updatedUser = userRepository.findById(userId).orElse(null);
            assertNotNull(updatedUser);
            assertEquals("nueva-imagen.jpg", updatedUser.getProfileImage());
        });
    }

    /**
     * Test actualizar info de host (crear nueva si no existe)
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testUpdateHostInfo_createNew_successful() {
        Long userId = 1L;
        HostDetailsUpdateDTO dto = new HostDetailsUpdateDTO(
            "Descripción del host",
            "documentos-legales.pdf"
        );

        assertDoesNotThrow(() -> {
            UserResponseDTO response = userService.updateHostInfo(userId, dto);
            assertNotNull(response);

            // Verificar en BD
            HostDetails hostDetails = hostDetailsRepository.findById(userId).orElse(null);
            assertNotNull(hostDetails);
            assertEquals("Descripción del host", hostDetails.getDescription());
            assertEquals("documentos-legales.pdf", hostDetails.getLegalDocumentsUrl());
        });
    }

    /**
     * Test actualizar info de host (actualizar existente)
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testUpdateHostInfo_updateExisting_successful() {
        Long userId = 1L;

        // Crear primero
        HostDetails initial = new HostDetails(userId, userRepository.findById(userId).get(), "Inicial", "inicial.pdf");
        hostDetailsRepository.save(initial);

        HostDetailsUpdateDTO dto = new HostDetailsUpdateDTO(
            "Descripción actualizada",
            "documentos-actualizados.pdf"
        );

        assertDoesNotThrow(() -> {
            UserResponseDTO response = userService.updateHostInfo(userId, dto);
            assertNotNull(response);

            // Verificar en BD
            HostDetails updated = hostDetailsRepository.findById(userId).orElse(null);
            assertNotNull(updated);
            assertEquals("Descripción actualizada", updated.getDescription());
            assertEquals("documentos-actualizados.pdf", updated.getLegalDocumentsUrl());
        });
    }

    /**
     * Test generar código de reset (nuevo, borra viejo si existe)
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testGenerateResetCode_successful() {
        String email = "carlos@gmail.com";

        // Crear un token viejo
        PasswordResetToken oldToken = new PasswordResetToken();
        oldToken.setEmail(email);
        oldToken.setCode("123456");
        oldToken.setExpiration(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(oldToken);

        assertDoesNotThrow(() -> {
            String code = userService.generateResetCode(email);
            assertNotNull(code);
            assertEquals(6, code.length());

            // Verificar borró viejo y guardó nuevo
            assertEquals(1, tokenRepository.findAllByEmail(email).size());
        });
    }

    /**
     * Test validar código de reset válido
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testValidateResetCode_valid_successful() {
        String email = "carlos@gmail.com";
        String code = userService.generateResetCode(email);

        boolean valid = userService.validateResetCode(email, code);
        assertTrue(valid);
    }

    /**
     * Test validar código de reset inválido
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testValidateResetCode_invalidCode_thenFalse() {
        String email = "carlos@gmail.com";
        userService.generateResetCode(email);  // Genera uno válido, pero usamos inválido

        boolean valid = userService.validateResetCode(email, "999999");
        assertFalse(valid);
    }

    /**
     * Test validar código de reset expirado
     */
    @Test
    @Sql("classpath:dataset.sql")
    void testValidateResetCode_expired_thenFalse() {
        String email = "carlos@gmail.com";

        // Crear token expirado
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setEmail(email);
        expiredToken.setCode("123456");
        expiredToken.setExpiration(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(expiredToken);

        boolean valid = userService.validateResetCode(email, "123456");
        assertFalse(valid);
    }
}