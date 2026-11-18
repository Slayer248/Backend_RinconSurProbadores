package cl.duocuc.probadores.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "probadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Probador {

    @Id
    @Column(nullable = false, unique = true, length = 15)
    private String codigo;

    @Column(name = "es_accesible", nullable = false)
    private Boolean esAccesible;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "LIBRE";

    @Column(name = "desde_hora", nullable = false, length = 15)
    @Builder.Default
    private String desdeHora = "-";

    @Column(name = "motivo_fuera_servicio", length = 100)
    private String motivoFueraServicio;
}