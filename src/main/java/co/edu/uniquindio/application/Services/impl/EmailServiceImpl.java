package co.edu.uniquindio.application.Services.impl;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Dtos.email.EmailDTO;
import co.edu.uniquindio.application.Services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.email.host}")
    private String SMTP_HOST;

    @Value("${app.email.port}")
    private Integer SMTP_PORT;

    @Value("${app.email.username}")
    private String SMTP_USERNAME;

    @Value("${app.email.password}")
    private String SMTP_PASSWORD;

    @Override
    @Async
    public void sendMail(EmailDTO emailDTO) throws Exception {
        Email email = EmailBuilder.startingBlank()
            .from("SMTP_USERNAME")
            .to(emailDTO.recipient())
            .withSubject(emailDTO.subject())
            .withPlainText(emailDTO.body())
            .buildEmail();

        try (Mailer mailer = MailerBuilder
            .withSMTPServer(SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD) //withHTMLText si usamos etiquetas html
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withDebugLogging(true)
            .buildMailer()) {
            
            mailer.sendMail(email);
        }
    }
}