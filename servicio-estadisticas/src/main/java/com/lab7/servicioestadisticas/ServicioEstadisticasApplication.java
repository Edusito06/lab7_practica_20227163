package com.lab7.servicioestadisticas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Consumidor (como Calculator en clase): usa servicio-horas via Feign + Eureka. */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServicioEstadisticasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioEstadisticasApplication.class, args);
    }
}
