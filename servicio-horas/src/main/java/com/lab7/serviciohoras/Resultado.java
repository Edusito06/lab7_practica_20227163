package com.lab7.serviciohoras;

/**
 * Respuesta de cada operacion. "puerto" dice que instancia atendio la peticion
 * (sirve para ver el balanceo de carga cuando hay 2 instancias).
 */
public record Resultado(String operacion, double valor, int puerto) {
}
