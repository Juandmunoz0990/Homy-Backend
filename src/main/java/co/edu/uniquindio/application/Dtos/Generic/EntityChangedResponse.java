package co.edu.uniquindio.application.Dtos.Generic;

import java.time.Instant;

public class EntityChangedResponse {
    private String message;
    private Instant timestamp;

    public EntityChangedResponse() {
    }

    public EntityChangedResponse(String message, Instant timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
}
