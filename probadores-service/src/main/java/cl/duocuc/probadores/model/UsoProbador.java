package cl.duocuc.probadores.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Cada vez que una persona ocupa un probador se guarda una fila: así queda el historial completo. */
@Entity
@Table(name = "usos_probador", indexes = @Index(name = "idx_usos_probador_codigo", columnList = "codigo_probador"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsoProbador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_probador", nullable = false, length = 15)
    private String codigoProbador;

    @Column(name = "numero_turno", length = 20)
    private String numeroTurno;

    /** Nombre del invitado que usó el probador. */
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDateTime inicio;

    /** Se completa cuando el probador vuelve a quedar libre; vacío mientras la persona sigue dentro. */
    private LocalDateTime fin;
}
