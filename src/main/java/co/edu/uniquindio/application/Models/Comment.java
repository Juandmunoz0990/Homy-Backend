package co.edu.uniquindio.application.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

    private Long bookingId; // Opcional
    private Long guestId;
    @ManyToOne
    @JoinColumn(name = "housing_id")
    private Housing housing;
    @Min(1)
    @Max(5)
    private Integer rate; // (1-5)
    @Column(columnDefinition = "TEXT")
    @Size(max = 500)
    private String content;
    @Size(max = 500)
    @Column(columnDefinition = "TEXT")
    private String hostReply;
    private LocalDateTime createdAt = LocalDateTime.now();
}