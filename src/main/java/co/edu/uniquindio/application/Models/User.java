package co.edu.uniquindio.application.Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

import co.edu.uniquindio.application.Models.enums.Role;

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
    @Enumerated(EnumType.STRING)
    private Role role;
    private Date birthDate;
    private String profileImage;
}
