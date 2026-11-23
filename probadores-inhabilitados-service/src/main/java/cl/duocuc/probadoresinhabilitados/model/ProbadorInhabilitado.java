package cl.duocuc.probadoresinhabilitados.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Copia histórica de un probador eliminado, con todas las personas que lo usaron. */
@Entity
@Table(name = "probadores_inhabilitados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProbadorInhabilitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código que tenía en probadores-service. Puede repetirse si se crea otro probador con el mismo código. */
    @Column(nullable = false, length = 15)
    private String codigo;

    @Column(name = "es_accesible", nullable = false)
    private Boolean esAccesible;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(name = "motivo_fuera_servicio", length = 100)
    private String motivoFueraServicio;

    @Column(name = "ultimo_cliente", length = 100)
    private String ultimoCliente;

    @Column(name = "ultimo_turno", length = 20)
    private String ultimoTurno;

    @Column(name = "ultimo_uso", length = 20)
    private String ultimoUso;

    @Column(name = "fecha_inhabilitacion", nullable = false)
    private LocalDateTime fechaInhabilitacion;

    @OneToMany(mappedBy = "probador", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("inicio ASC")
    @Builder.Default
    private List<UsoProbadorArchivado> usos = new ArrayList<>();
}
