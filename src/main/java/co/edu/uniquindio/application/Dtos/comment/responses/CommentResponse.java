package co.edu.uniquindio.application.Dtos.comment.responses;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentResponse {
    private String guestName;
    private String housingTitle;
    private Long housingId;
    private Integer rate;
    private String content;
    private String hostReply;
    private LocalDateTime createdAt = LocalDateTime.now();
    
}
