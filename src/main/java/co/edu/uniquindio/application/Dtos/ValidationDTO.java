package co.edu.uniquindio.application.Dtos;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String field;
    private String message;
}