package cl.duocuc.usuarios.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:soporte.rinconsur@gmail.com}")
    private String remitente;

    public void enviarCredenciales(String destinatario, String nombre, String username, String contrasena, String rol) {
        log.info("Enviando correo con credenciales a {}...", destinatario);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Bienvenido a Rincón Sur - Credenciales de acceso");

        String cuerpo = String.format(
                """
                Hola %s,
                
                Se ha creado una cuenta para ti en el sistema de Rincón Sur con el rol: %s.
                
                Tus credenciales de acceso son:
                --------------------------------------------------
                Usuario:    %s
                Contraseña: %s
                --------------------------------------------------
                
                Puedes iniciar sesión en la aplicación móvil con estas credenciales.
                Te sugerimos cambiar tu contraseña después de tu primer ingreso.
                
                Atentamente,
                Equipo de Soporte Rincón Sur
                """,
                nombre, rol, username, contrasena
        );

        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
        log.info("Correo enviado exitosamente a {}", destinatario);
    }

    public void enviarRecuperacionContrasena(String destinatario, String nombre, String username, String contrasenaTemporal) {
        log.info("Enviando correo de recuperación de contraseña a {}...", destinatario);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Rincón Sur - Recuperación de Contraseña");

        String cuerpo = String.format(
                """
                Hola %s,
                
                Has solicitado restablecer tu contraseña para la cuenta '%s' en Rincón Sur.
                
                Tu nueva contraseña temporal de acceso es:
                --------------------------------------------------
                Contraseña temporal: %s
                --------------------------------------------------
                
                Inicia sesión con esta contraseña en la aplicación móvil y cámbiala desde la opción 'Cambiar Contraseña'.
                
                Si tú no solicitaste este cambio, por favor contacta al administrador del sistema.
                
                Atentamente,
                Equipo de Soporte Rincón Sur
                """,
                nombre, username, contrasenaTemporal
        );

        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
        log.info("Correo de recuperación enviado exitosamente a {}", destinatario);
    }
}
