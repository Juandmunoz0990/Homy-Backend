package co.edu.uniquindio.application.Services;

import java.util.Optional;

import co.edu.uniquindio.application.Models.User;

public interface UserService {
    User register(User u);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}