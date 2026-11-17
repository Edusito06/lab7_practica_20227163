# Lab 7 (práctica): Lab 6 + microservicios con Eureka

```
App Android (Retrofit) ──► servicio-estadisticas :8020 ──Feign "servicio-horas"──► servicio-horas :8010 / :8011
                                    │  circuit breaker + fallback                      │
                                    └────────────── se registran en ──► eureka-server :8761 ◄┘
```

| Proyecto | Puerto | Rol |
|---|---|---|
| `eureka-server` | 8761 | Registro y descubrimiento (dashboard en http://localhost:8761) |
| `servicio-horas` | 8010 (y 8011) | Proveedor: `/horas/sumar/{a}/{b}`, `/horas/promedio/{total}/{n}`, `/horas/minutos/{h}`, `/horas/faltante/{h}/{meta}` |
| `servicio-estadisticas` | 8020 | Consumidor con Feign: `/estadisticas/sumar/{a}/{b}`, `/estadisticas/resumen?horas=2,1.5&meta=20` |
| `app-android` | — | Lab 6 + tarjeta "Resumen (microservicios)" en la pantalla de Gráficos |

## Arrancar (en este orden, cada uno en su terminal)

```bash
cd eureka-server && ./mvnw spring-boot:run
```
```bash
cd servicio-horas && ./mvnw spring-boot:run
```
```bash
cd servicio-horas && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8011
```
```bash
cd servicio-estadisticas && ./mvnw spring-boot:run
```

Si `java` no está en el PATH: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.

## Pruebas rápidas

- http://localhost:8761: deben aparecer SERVICIO-HORAS (2 instancias) y SERVICIO-ESTADISTICAS.
- http://localhost:8020/estadisticas/sumar/2/1.5: recarga varias veces y el puerto alterna 8010/8011 (balanceo).
- Apaga las 2 instancias de servicio-horas: `/estadisticas/resumen` responde `disponible:false` (fallback).
  Tras 3 fallos el circuito se abre (`CallNotPermittedException`) durante 10 s.
- http://localhost:8010/actuator/info y `/actuator/health`.

## App Android

- URL base en `app/src/main/java/com/lab6/iot/api/ApiClient.java`: `http://10.0.2.2:8020/` (emulador).
  En un celular físico, la IP de tu PC en la misma red Wi-Fi.
- `android:usesCleartextTraffic="true"` en el Manifest (sin esto Android bloquea `http://`).
- Cambios respecto al Lab 6: Gradle 9.2.1 / AGP 8.13.1 (funciona con el JDK 25 de Android Studio),
  Retrofit + Gson, paquete `api/` y la tarjeta en `activity_charts.xml` / `ChartsActivity`.
