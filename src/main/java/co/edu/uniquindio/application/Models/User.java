package co.edu.uniquindio.application.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
    private Date birthDate;

    public enum Role {
        HOST, GUEST;
    }
}
