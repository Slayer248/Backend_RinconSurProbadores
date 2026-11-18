package cl.duocuc.turnos.dto;

public record CrearTurnoRequest(
        String nombreCliente,
        Integer cantidadPrendas,
        Boolean esPreferente
) {}