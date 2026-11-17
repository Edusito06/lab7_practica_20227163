package com.lab7.servicioestadisticas;

/** Mismo formato JSON que devuelve servicio-horas. */
public record Resultado(String operacion, double valor, int puerto) {
}
