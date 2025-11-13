package co.edu.uniquindio.application.Services;


import co.edu.uniquindio.application.Dtos.User.ChangePasswordRequest;
import co.edu.uniquindio.application.Dtos.User.HostDetailsUpdateDTO;
import co.edu.uniquindio.application.Dtos.User.UserResponseDTO;
import co.edu.uniquindio.application.Dtos.User.UserUpdateDTO;
import co.edu.uniquindio.application.Dtos.auth.RegisterRequest;
import co.edu.uniquindio.application.Models.User;

public interface UserService {
    User register(RegisterRequest u);

    User findByEmail(String email);

    User findById(Long id);

    UserResponseDTO updateUser(Long userId, UserUpdateDTO dto);

    UserResponseDTO updateHostInfo(Long userId, HostDetailsUpdateDTO dto);

    String generateResetCode(String email);

    boolean validateResetCode(String email, String code);

    void changePassword(Long userId, ChangePasswordRequest request);
}