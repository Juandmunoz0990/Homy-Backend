package co.edu.uniquindio.application.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByEmailAndCode(String email, String code);
    
    void deleteByEmail(String email);

    List<PasswordResetToken> findAllByEmail(String email);

    Optional<PasswordResetToken> findByEmail(String email);
}