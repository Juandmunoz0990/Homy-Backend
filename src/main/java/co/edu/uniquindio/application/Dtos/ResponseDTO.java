package co.edu.uniquindio.application.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDTO<T> {

    private boolean success;
    private T content;
}
