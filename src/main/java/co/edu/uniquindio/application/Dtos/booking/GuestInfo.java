package co.edu.uniquindio.application.Dtos.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GuestInfo {
    
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}
