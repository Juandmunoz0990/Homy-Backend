package co.edu.uniquindio.application.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-seconds}")
    private long expirationSeconds;

    public String generateToken(Authentication auth) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationSeconds * 1000);
        return Jwts.builder()
                .setSubject(auth.getName())
                .claim("userId", ((CustomUserDetails) auth.getPrincipal()).getId())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}