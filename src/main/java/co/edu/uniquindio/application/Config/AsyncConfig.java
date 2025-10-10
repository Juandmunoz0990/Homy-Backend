package co.edu.uniquindio.application.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync

// Configuracion para habilitar el soporte de metodos asincronos
public class AsyncConfig implements AsyncConfigurer {
}