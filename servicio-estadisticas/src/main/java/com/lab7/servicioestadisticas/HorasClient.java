package com.lab7.servicioestadisticas;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * name = spring.application.name del proveedor (EXACTO).
 * No hay IP ni puerto: Eureka dice donde esta y el load balancer reparte entre instancias.
 * Las rutas y los @PathVariable("...") deben calzar con HorasController.
 */
@FeignClient(name = "servicio-horas")
public interface HorasClient {

    @GetMapping("/horas/sumar/{a}/{b}")
    Resultado sumar(@PathVariable("a") double a, @PathVariable("b") double b);

    @GetMapping("/horas/promedio/{total}/{n}")
    Resultado promedio(@PathVariable("total") double total, @PathVariable("n") int n);

    @GetMapping("/horas/minutos/{horas}")
    Resultado minutos(@PathVariable("horas") double horas);

    @GetMapping("/horas/faltante/{horas}/{meta}")
    Resultado faltante(@PathVariable("horas") double horas, @PathVariable("meta") double meta);
}
