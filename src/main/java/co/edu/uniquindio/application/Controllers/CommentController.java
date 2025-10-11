package co.edu.uniquindio.application.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.comment.requests.CommentRequest;
import co.edu.uniquindio.application.Dtos.comment.responses.CommentResponse;
import co.edu.uniquindio.application.Services.CommentService;

import java.util.List;

@RestController
@RequestMapping("/housings/{housingId}/comments")
public class CommentController {

    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> toList(@PathVariable Long housingId) {
        List<CommentResponse> response = service.findByHousingId(housingId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('GUEST')")
    @PostMapping("/create")
    public ResponseEntity<EntityCreatedResponse> create(@RequestBody CommentRequest request, @AuthenticationPrincipal User user) {
        Long guestId = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        EntityCreatedResponse created = service.create(guestId, request);
        return ResponseEntity.status(201).body(created);
    }

    @PreAuthorize("hasAuthority('HOST')")
    @PostMapping("/{commentId}")
    public void replyComment(@AuthenticationPrincipal User user, @PathVariable Long commentId, String message){
        Long hostId = user.getUsername() != null ? Long.parseLong(user.getUsername()) : null;
        service.replyComment(hostId, commentId, message);
    }
}