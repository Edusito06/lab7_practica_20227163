package com.lab7.servicioestadisticas;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/estadisticas")
public class EstadisticasController {

    private final HorasClient horasClient;
    private final CircuitBreaker circuitBreaker;

    public EstadisticasController(HorasClient horasClient, CircuitBreakerFactory<?, ?> cbFactory) {
        this.horasClient = horasClient;
        this.circuitBreaker = cbFactory.create("servicio-horas");
    }

    /** Igual que /addcalculator de la clase: http://localhost:8020/estadisticas/sumar/2/1.5 */
    @GetMapping("/sumar/{a}/{b}")
    public String sumar(@PathVariable("a") double a, @PathVariable("b") double b) {
        return circuitBreaker.run(
                () -> {
                    Resultado r = horasClient.sumar(a, b);
                    return "Estadisticas: " + r.operacion() + " = " + r.valor() + " h (atendido por puerto " + r.puerto() + ")";
                },
                error -> "Estadisticas: servicio-horas no disponible (" + error.getClass().getSimpleName() + ")");
    }

    /**
     * Lo que usa la app Android:
     * http://localhost:8020/estadisticas/resumen?horas=2,1.5,3&meta=20
     */
    @GetMapping("/resumen")
    public Resumen resumen(@RequestParam(name = "horas", required = false) List<Double> horas,
                           @RequestParam(name = "meta", defaultValue = "20") double meta) {
        List<Double> lista = horas == null ? List.of() : horas;
        return circuitBreaker.run(
                () -> calcular(lista, meta),
                // FALLBACK: si servicio-horas esta caido o tarda demasiado
                error -> Resumen.noDisponible(error.getClass().getSimpleName()));
    }

    private Resumen calcular(List<Double> horas, double meta) {
        List<Integer> puertos = new ArrayList<>();

        // Suma acumulada llamando al proveedor (como Calculator -> Addition)
        double total = 0;
        for (double h : horas) {
            Resultado r = horasClient.sumar(total, h);
            total = r.valor();
            puertos.add(r.puerto());
        }

        Resultado prom = horasClient.promedio(total, horas.size());
        Resultado min = horasClient.minutos(total);
        Resultado falta = horasClient.faltante(total, meta);
        puertos.add(prom.puerto());
        puertos.add(min.puerto());
        puertos.add(falta.puerto());

        return new Resumen(true, "OK", horas.size(), total, min.valor(),
                prom.valor(), meta, falta.valor(), puertos);
    }
}
