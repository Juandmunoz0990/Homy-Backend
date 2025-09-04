package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.application.Models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}