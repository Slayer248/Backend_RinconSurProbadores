package cl.duocuc.cuentasinhabilitadas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Copia histórica de una cuenta que el administrador eliminó. Nunca se borra ni se reactiva. */
@Entity
@Table(name = "cuentas_inhabilitadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaInhabilitada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID que tenía la cuenta en usuarios-service. */
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String correo;

    /** Hash de la contraseña tal como estaba; nunca se expone por la API. */
    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(name = "rol_nombre", length = 50)
    private String rolNombre;

    @Column(name = "bloqueada_antes", nullable = false)
    private Boolean bloqueadaAntes;

    @Column(name = "inhabilitada_por_id")
    private Long inhabilitadaPorId;

    @Column(name = "fecha_inhabilitacion", nullable = false)
    private LocalDateTime fechaInhabilitacion;
}
