package com.lab6.iot;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

/**
 * Modelo guardado en Cloud Firestore.
 * Ruta: usuarios/{uid}/sesiones/{id}
 */
public class SesionEstudio {

    public static final String[] TIPOS = {"Teoría", "Práctica", "Laboratorio"};

    @DocumentId
    public String id;              // no se guarda como campo, es el ID del documento
    public String curso;
    public Date fecha;             // se guarda como Timestamp
    public double horasDedicadas;
    public String tipoActividad;   // Teoría, Práctica o Laboratorio

    // Constructor vacio requerido por Firestore
    public SesionEstudio() {
    }

    public SesionEstudio(String curso, Date fecha, double horasDedicadas, String tipoActividad) {
        this.curso = curso;
        this.fecha = fecha;
        this.horasDedicadas = horasDedicadas;
        this.tipoActividad = tipoActividad;
    }

    /** 2.0 -> "2", 1.5 -> "1.5" */
    public static String formatoHoras(double h) {
        if (h == Math.floor(h)) return String.valueOf((long) h);
        return String.valueOf(h);
    }

    /** Color del chip / grafico segun el tipo de actividad */
    public static int colorFondo(String tipo) {
        if ("Laboratorio".equals(tipo)) return 0xFFDDF3E6;
        if ("Práctica".equals(tipo)) return 0xFFFDE9D7;
        return 0xFFE3E8F8; // Teoría
    }

    public static int colorTexto(String tipo) {
        if ("Laboratorio".equals(tipo)) return 0xFF1E7A46;
        if ("Práctica".equals(tipo)) return 0xFFB45309;
        return 0xFF1E3A8A; // Teoría
    }

    public static int colorGrafico(String tipo) {
        if ("Laboratorio".equals(tipo)) return 0xFF2E9D5B;
        if ("Práctica".equals(tipo)) return 0xFFF08C2E;
        return 0xFF1E4FA3; // Teoría
    }
}
