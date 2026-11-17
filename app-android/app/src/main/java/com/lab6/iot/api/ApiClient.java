package com.lab6.iot.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit apuntando a servicio-estadisticas (puerto 8020).
 * - Emulador: 10.0.2.2 es el "localhost" de tu computadora.
 * - Celular fisico: usa la IP de tu PC en la misma red Wi-Fi, ej. http://192.168.1.50:8020/
 * La app NO llama directo a servicio-horas: eso lo hace estadisticas con Feign + Eureka.
 */
public final class ApiClient {

    public static final String BASE_URL = "http://10.0.2.2:8020/";

    private static EstadisticasApi api;

    private ApiClient() {
    }

    public static EstadisticasApi estadisticas() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // debe terminar en "/"
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(EstadisticasApi.class);
        }
        return api;
    }
}
