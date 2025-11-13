package co.edu.uniquindio.application.Services.impl;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Pattern;

import org.hibernate.ObjectNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.application.Dtos.User.ChangePasswordRequest;
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
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final HostDetailsRepository hostInfoRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    @Override
    public User register(RegisterRequest u) {
        boolean exists = repo.findByEmail(u.email()).isPresent();
        if(exists) throw new IllegalArgumentException("Email already in use");

        validatePasswordStrength(u.password());

        String pasEncode = passwordEncoder.encode(u.password());
        User user = userMapper.toUser(u);
        user.setPassword(pasEncode);
        return repo.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new ObjectNotFoundException("User with email: " + email +" not found", User.class));
    }

    @Override
    public User findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ObjectNotFoundException("User with id: " + id + " not found" , User.class));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long userId, UserUpdateDTO dto) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(dto.name());
        user.setPhoneNumber(dto.phoneNumber());
        user.setProfileImage(dto.profileImageUrl());

        repo.save(user);
        return new UserResponseDTO(
                user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(),
                user.getProfileImage(), user.getRole()
        );
    }

    @Override
    @Transactional
    public UserResponseDTO updateHostInfo(Long userId, HostDetailsUpdateDTO dto) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        HostDetails hostInfo = hostInfoRepository.findById(userId)
                .orElse(new HostDetails(userId, user, null, null));

        hostInfo.setDescription(dto.description());
        hostInfo.setLegalDocumentsUrl(dto.legalDocumentsUrl());

        hostInfoRepository.save(hostInfo);

        return new UserResponseDTO(
                user.getId(), user.getName(), user.getEmail(),
                user.getPhoneNumber(), user.getProfileImage(), user.getRole()
        );
    }

    @Transactional //Por el delete
    @Override
    public String generateResetCode(String email) { //Genera y guarda el token de restablecimiento
        // Elimina cualquier código anterior
        tokenRepository.deleteByEmail(email);

        String code = String.format("%06d", new Random().nextInt(999999));
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setExpiration(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        return code;
    }

    @Override
    public boolean validateResetCode(String email, String code) { //Cuando pongo el código en la página
        return tokenRepository.findByEmailAndCode(email, code)
                .filter(token -> token.getExpiration().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User with id: " + userId + " not found", User.class));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        validatePasswordStrength(request.newPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repo.save(user);
    }

    private void validatePasswordStrength(String rawPassword) {
        if (rawPassword == null || !STRONG_PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, una mayúscula y un número");
        }
    }
}
