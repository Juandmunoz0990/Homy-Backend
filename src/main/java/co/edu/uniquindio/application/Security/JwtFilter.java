package co.edu.uniquindio.application.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter{
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        if (path.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        // Obtener el token del header de la solicitud
        String token = getToken(request);
        
        // Si no hay token, continuar con la cadena de filtros
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // Validar el token y obtener el payload
            Jws<Claims> payload = jwtUtil.parseJwt(token);
            String username = payload.getPayload().getSubject();
            
            // Si el usuario no está autenticado, crear un nuevo objeto de autenticación
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Crear un objeto UserDetails con el nombre de usuario y el rol
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Crear un objeto de autenticación y establecerlo en el contexto de seguridad
                UsernamePasswordAuthenticationToken authentication = new 
                        UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Si el token no es válido, enviar un error 401
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            // return;
        }
        
        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }
    
    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.replace("Bearer ", "") : null;
    }
}