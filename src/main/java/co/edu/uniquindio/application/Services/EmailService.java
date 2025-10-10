package co.edu.uniquindio.application.Services;

import co.edu.uniquindio.application.Dtos.email.EmailDTO;

public interface EmailService {
    
    void sendMail(EmailDTO emailDTO) throws Exception;
}
