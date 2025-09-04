package co.edu.uniquindio.application.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservaId; // Opcional
    private Long guestId;
    private Long housingId;
    private Integer rate; // (1-5)
    @Column(columnDefinition = "TEXT")
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String hostResponse;
}