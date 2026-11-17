package com.lab7.servicioestadisticas;

import java.util.List;

/** Lo que recibe la app Android. */
public record Resumen(
        boolean disponible,      // false = respondio el fallback del circuit breaker
        String mensaje,
        int sesiones,
        double totalHoras,
        double totalMinutos,
        double promedio,
        double meta,
        double faltante,
        List<Integer> atendidoPor // puertos de servicio-horas que respondieron (balanceo)
) {
    static Resumen noDisponible(String motivo) {
        return new Resumen(false, "servicio-horas no disponible: " + motivo,
                0, 0, 0, 0, 0, 0, List.of());
    }
}
