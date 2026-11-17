# Indicaciones para continuar en la PC del laboratorio

> Para el asistente que abra esta sesión: lee este archivo completo antes de hacer nada.
> Aquí está todo el contexto de lo que ya se hizo, lo que falta y las reglas a respetar.

## 1. Contexto

- Curso: **1TEL05 Servicios y Aplicaciones para IoT (PUCP, 2026-2)**. Alumno: código **20227163**.
- **Lab 6** (ya entregado, nota 16): app Android (Java) "Sistema de Registro de Horas de Estudio"
  con Firebase UI (correo + Google), Cloud Firestore (`usuarios/{uid}/sesiones/{id}` con `curso`,
  `fecha` Timestamp, `horasDedicadas`, `tipoActividad` Teoría/Práctica/Laboratorio), CRUD en tiempo
  real y MPAndroidChart (barras por mes + torta por tipo, filtro por curso). Proyecto Firebase: **`lab6-20227163`**.
- **Lab 7** (el de hoy): tema **microservicios con Eureka**, usando el Lab 6 como base.
  Clase 12 del curso (Prof. Oscar Díaz) con estos repos de referencia:
  - https://github.com/2026-2-1TEL05-AppsIot/Clase12_EurekaServer (puerto 8761)
  - https://github.com/2026-2-1TEL05-AppsIot/Clase12_EurekaClient_Hello (8001, `/greeting`)
  - https://github.com/2026-2-1TEL05-AppsIot/Clase12_EurekaClient_Addition (8010, `/addition/{a}/{b}`)
  - https://github.com/2026-2-1TEL05-AppsIot/Clase12_EurekaClient_Calculator (8020, Feign → Addition, `/addcalculator/{a}/{b}`)
  - Versiones del curso: **Spring Boot 4.1.2, Spring Cloud 2025.1.4, Java 17**.
  - Pistas de la clase: el diagrama muestra un servicio "Substract" que no está en los repos,
    `instance-id` con `${random.int}` (varias instancias / balanceo), Calculator trae Resilience4j
    sin usar (circuit breaker + fallback) y Actuator expuesto (`/actuator/health`, `/actuator/info`).

## 2. Qué contiene este repo (práctica, ya probada en la Mac)

```
App Android (Retrofit) ──► servicio-estadisticas :8020 ──Feign "servicio-horas"──► servicio-horas :8010 / :8011
                                  │ circuit breaker + fallback                         │
                                  └─────────── se registran en ──► eureka-server :8761 ◄┘
```

| Carpeta | Puerto | Rol |
|---|---|---|
| `eureka-server` | 8761 | `@EnableEurekaServer`, no se registra a sí mismo |
| `servicio-horas` | 8010 (+8011) | Proveedor (como Addition): `/horas/sumar/{a}/{b}`, `/horas/promedio/{total}/{n}`, `/horas/minutos/{h}`, `/horas/faltante/{h}/{meta}`. Devuelve `{operacion, valor, puerto}` |
| `servicio-estadisticas` | 8020 | Consumidor (como Calculator): `@FeignClient(name="servicio-horas")`, `CircuitBreakerFactory`, `/estadisticas/sumar/{a}/{b}` y `/estadisticas/resumen?horas=2,1.5&meta=20` |
| `app-android` | — | Lab 6 + tarjeta "Resumen (microservicios)" en Gráficos (`ChartsActivity`), paquete `com.lab6.iot.api` (Retrofit + Gson) |

**Verificado en la Mac:** los 3 servicios aparecen `UP` en Eureka, el balanceo alterna 8010/8011,
el fallback responde `disponible:false` con servicio-horas apagado y el circuito se abre
(`CallNotPermittedException`) tras 3 fallos. La app **compila** (`assembleDebug`), pero **aún no se ha
probado en un emulador** (el de la Mac era muy lento). Probarla es lo primero que hay que hacer aquí.

Cambios respecto al Lab 6 original: Gradle 9.2.1 + AGP 8.13.1, Retrofit 2.11.0 + converter-gson,
`android:usesCleartextTraffic="true"` en el Manifest, `BASE_URL = http://10.0.2.2:8020/` en `ApiClient.java`.

## 3. Puesta en marcha en la PC del lab (probablemente Windows)

1. **Java:** usar el JDK de Android Studio si no hay otro (PowerShell):
   `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"` (verificar la ruta real) y
   comprobar con `& "$env:JAVA_HOME\bin\java" -version`. Se necesita Java 17 o superior.
2. **Levantar los servicios en este orden**, cada uno en su terminal (en Windows es `mvnw.cmd`):
   - `cd eureka-server` → `.\mvnw.cmd spring-boot:run`
   - `cd servicio-horas` → `.\mvnw.cmd spring-boot:run`
   - `cd servicio-horas` → `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8011"`
   - `cd servicio-estadisticas` → `.\mvnw.cmd spring-boot:run`
   - La primera vez Maven descarga dependencias (necesita internet). Si el Firewall de Windows pregunta por Java, se debe **Permitir**.
3. **Verificar:** http://localhost:8761 (3 instancias), http://localhost:8020/estadisticas/sumar/2/1.5
   (el puerto alterna) y http://localhost:8020/estadisticas/resumen?horas=2,1.5,3&meta=20.
4. **SHA-1 de esta PC en Firebase** (para el login con Google): en `app-android` correr
   `.\gradlew.bat signingReport`, copiar el SHA1 de `debug` y agregarlo en la consola de Firebase →
   Configuración del proyecto → app `com.lab6.iot` → Agregar huella digital. Esto lo hace el alumno en la consola.
   Mientras tanto, el login con **correo/contraseña** funciona sin el SHA-1.
5. **App:** abrir `app-android` en Android Studio, emulador encendido, Run. Iniciar sesión → Ver gráficos →
   **Calcular**. Debe mostrar sesiones, total, minutos, promedio, horas faltantes y los puertos que atendieron.
   Para probar el fallback, detener las 2 instancias de servicio-horas y volver a tocar Calcular.

## 4. Problemas conocidos y solución

| Síntoma | Causa / solución |
|---|---|
| Android Studio no sincroniza: "requires Android Studio ... / AGP 8.13.1" | El Android Studio del lab es más antiguo. Bajar en `build.gradle` a AGP `8.5.2` y en `gradle-wrapper.properties` a `gradle-8.7-bin`, con Gradle JDK 17 o 21 |
| `Unsupported class file major version 69` | Gradle 8.x corriendo con Java 25. Usar Gradle 9.2.1 (ya configurado) o cambiar el Gradle JDK a 17/21 |
| Google Sign-In error 10 | Falta el SHA-1 de esta PC en Firebase (paso 4) |
| La app dice "No se pudo conectar con servicio-estadisticas" | Servicio apagado, URL mala (en el emulador es `10.0.2.2`, en un celular físico la IP de la PC) o falta `usesCleartextTraffic` |
| Feign falla con "Load balancer does not contain an instance" | El proveedor aún no aparece en Eureka (esperar ~30 s) o el `name` del `@FeignClient` no coincide con su `spring.application.name` |
| Nombre de servicio con `_` falla al llamarlo por Feign | Feign usa el nombre como host: usar guiones (`servicio-horas`), nunca guion bajo |
| Puerto ocupado | `netstat -ano | findstr :8010` y cerrar el proceso, o cambiar `server.port` |

## 5. Cómo adaptar al enunciado real del Lab 7

Leer primero el enunciado completo y comparar con lo que ya hay. Lo más probable es que pida alguna combinación de:
- **Nuevos Eureka Clients** (por ejemplo una resta, conversiones o validaciones): copiar `servicio-horas` como plantilla
  (pom, `application.yaml` con nombre y puerto nuevos, controller).
- **Consumir con Feign:** agregar métodos o interfaces `@FeignClient` en `servicio-estadisticas`. Las rutas y los
  `@PathVariable("...")` deben coincidir exactamente con el proveedor.
- **Varias instancias + balanceo:** ya está resuelto con `instance-id: ${spring.application.name}:${server.port}`.
- **Circuit breaker / fallback:** el patrón ya está en `EstadisticasController` (`circuitBreaker.run(..., error -> ...)`).
- **Gateway (Spring Cloud Gateway):** un proyecto nuevo con `spring-cloud-starter-gateway` + eureka-client y rutas `uri: lb://nombre-servicio`.
- **App Android:** nuevos endpoints en `EstadisticasApi` y su uso en la pantalla que pida el enunciado.
- Respetar los **nombres, puertos y rutas que diga el enunciado**, aunque difieran de esta práctica. El enunciado manda.

Entregables típicos del curso (confirmar en el enunciado): repo GitHub **público** con el nombre que indiquen
(estilo `LAB7_20227163`), Firebase compartido con la JP si se usa, y capturas del dashboard de Eureka si las piden.

## 6. Reglas para el asistente

- **Commits y push solo como el alumno**: no agregar líneas `Co-Authored-By`, firmas, menciones a herramientas de IA
  ni "Generated with ..." en commits, PRs, README ni comentarios de código. El alumno no quiere aparecer con colaboradores.
- No crear archivos con nombres que delaten asistentes (por ejemplo `CLAUDE.md`).
- Explicar en español y de forma breve qué se cambia y por qué; el alumno debe poder defender el código.
- No tocar la consola de Firebase ni crear cuentas: indicar al alumno qué hacer.
