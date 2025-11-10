package co.edu.uniquindio.application.Security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import co.edu.uniquindio.application.Models.User;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final Long id;
    private final String email;

    public CustomUserDetails(User user) {
        super(user.getId().toString(), user.getPassword(),
              List.of(new SimpleGrantedAuthority(user.getRole().name())));
        this.id = user.getId();
        this.email = user.getEmail();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
}
