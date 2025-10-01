package co.edu.uniquindio.application.Dtos.Generic;

import java.time.Instant;

public record EntityCreatedResponse (
    String message,
    Instant timestamp
){
    

}
