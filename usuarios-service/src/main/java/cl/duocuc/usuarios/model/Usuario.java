package cl.duocuc.usuarios.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bloqueado = false;

    /** Se activa con contraseñas temporales: cuenta nueva o recuperación de contraseña. */
    @Column(name = "debe_cambiar_contrasena", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Boolean debeCambiarContrasena = false;
}