package com.lab7.serviciohoras;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/horas")
public class HorasController {

    // Puerto real de esta instancia (8010, 8011, ...)
    @Value("${server.port}")
    private int puerto;

    /** http://localhost:8010/horas/sumar/2/1.5 */
    @GetMapping("/sumar/{a}/{b}")
    public Resultado sumar(@PathVariable("a") double a, @PathVariable("b") double b) {
        return new Resultado(a + " + " + b, redondear(a + b), puerto);
    }

    /** http://localhost:8010/horas/promedio/6.5/3 */
    @GetMapping("/promedio/{total}/{n}")
    public Resultado promedio(@PathVariable("total") double total, @PathVariable("n") int n) {
        double prom = n == 0 ? 0 : total / n;
        return new Resultado(total + " / " + n, redondear(prom), puerto);
    }

    /** http://localhost:8010/horas/minutos/2.5 */
    @GetMapping("/minutos/{horas}")
    public Resultado minutos(@PathVariable("horas") double horas) {
        return new Resultado(horas + " h a minutos", Math.round(horas * 60), puerto);
    }

    /** http://localhost:8010/horas/faltante/6.5/20  -> horas que faltan para la meta */
    @GetMapping("/faltante/{horas}/{meta}")
    public Resultado faltante(@PathVariable("horas") double horas, @PathVariable("meta") double meta) {
        return new Resultado("meta " + meta + " - " + horas, redondear(Math.max(0, meta - horas)), puerto);
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
