package cl.duocuc.turnos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_turno", nullable = false, unique = true, length = 20)
    private String numeroTurno;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(name = "cantidad_prendas", nullable = false)
    private Integer cantidadPrendas;

    @Column(name = "es_preferente", nullable = false)
    private Boolean esPreferente;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "EN_ESPERA";
}