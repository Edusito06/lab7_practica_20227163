package com.lab6.iot.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EstadisticasApi {

    /** GET /estadisticas/resumen?horas=2,1.5,3&meta=20 */
    @GET("estadisticas/resumen")
    Call<Resumen> resumen(@Query("horas") String horasSeparadasPorComa,
                          @Query("meta") double meta);
}
