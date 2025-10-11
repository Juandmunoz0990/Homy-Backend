package co.edu.uniquindio.application.Dtos.comment.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull
    private Long bookingId; 

    @NotNull
    private Long housingId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rate; 

    @NotBlank
    @Size(max = 500)
    private String content;
    
}
