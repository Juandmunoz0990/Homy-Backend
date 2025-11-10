package co.edu.uniquindio.application.Security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        log.info("Validando credenciales de: {}", user.getEmail());

        return new CustomUserDetails(user);
    }
}