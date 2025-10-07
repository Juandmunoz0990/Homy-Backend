package co.edu.uniquindio.application.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Dtos.LoginRequest;
import co.edu.uniquindio.application.Dtos.RegisterRequest;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Security.JwtUtil;
import co.edu.uniquindio.application.Services.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        User u = new User();
        u.setName(req.getNombre());
        u.setEmail(req.getEmail());
        u.setPassword(req.getPassword());
        u.setPhoneNumber(req.getTelefono());
        u.setRole(req.getRole());
        var saved = usuarioService.register(u);
        return ResponseEntity.status(201).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var opt = usuarioService.findByEmail(req.getEmail());
        if (opt.isEmpty())
            return ResponseEntity.status(401).body("Credenciales inválidas");
        var u = opt.get();
        if (!u.getPassword().equals(req.getPassword()))
            return ResponseEntity.status(401).body("Credenciales inválidas");

        org.springframework.security.core.Authentication authentication =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                u.getEmail(), u.getPassword(), java.util.Collections.emptyList());
        var token = jwtUtil.generateToken(authentication);
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {
            {
                put("accessToken", token);
                put("tokenType", "Bearer");
            }
        });
    }
}