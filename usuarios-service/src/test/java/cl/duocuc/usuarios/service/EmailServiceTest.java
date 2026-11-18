package cl.duocuc.usuarios.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void enviarCredencialesYRecuperacion_EnvianCorreosConJavaMailSender() {
        ReflectionTestUtils.setField(emailService, "remitente", "soporte.rinconsur@gmail.com");

        emailService.enviarCredenciales("user@test.cl", "Juan", "jperez", "Pass1234@", "ASISTENTE");
        emailService.enviarRecuperacionContrasena("user@test.cl", "Juan", "jperez", "Temp1234@");

        verify(mailSender, org.mockito.Mockito.times(2)).send(any(SimpleMailMessage.class));
    }
}