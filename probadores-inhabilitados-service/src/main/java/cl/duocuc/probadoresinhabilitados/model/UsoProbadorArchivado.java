package cl.duocuc.probadoresinhabilitados.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Una persona que usó el probador antes de que fuera eliminado. */
@Entity
@Table(name = "usos_probador_archivados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsoProbadorArchivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "probador_inhabilitado_id", nullable = false)
    private ProbadorInhabilitado probador;

    @Column(name = "codigo_probador", nullable = false, length = 15)
    private String codigoProbador;

    @Column(name = "numero_turno", length = 20)
    private String numeroTurno;

    /** Nombre del invitado que usó el probador. */
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDateTime inicio;

    private LocalDateTime fin;
}
