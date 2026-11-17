package com.lab6.iot.api;

import java.util.List;

/** Mismo JSON que devuelve servicio-estadisticas (los nombres deben coincidir para Gson). */
public class Resumen {
    public boolean disponible;
    public String mensaje;
    public int sesiones;
    public double totalHoras;
    public double totalMinutos;
    public double promedio;
    public double meta;
    public double faltante;
    public List<Integer> atendidoPor;
}
