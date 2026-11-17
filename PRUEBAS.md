# Pruebas: Lab 7 (microservicios con Eureka)

Hacer las pruebas en orden. Si una falla, no seguir con la siguiente: revisar la tabla del final.
Todas las URL se pueden abrir en el navegador. En PowerShell usar `curl.exe` (con **.exe**), porque
`curl` solo es otro comando de PowerShell.

Antes de empezar, deben estar corriendo en este orden: `eureka-server`, `servicio-horas` (8010),
`servicio-horas` (8011) y `servicio-estadisticas` (8020).

## Parte 1: Microservicios (sin la app)

### 1. Eureka
Abrir http://localhost:8761

✅ En "Instances currently registered" aparecen:
- **SERVICIO-HORAS** con 2 instancias: `servicio-horas:8010` y `servicio-horas:8011`
- **SERVICIO-ESTADISTICAS**: `servicio-estadisticas:8020`

Todas en `UP`.

### 2. Proveedor directo (servicio-horas)
| URL | Resultado esperado |
|---|---|
| http://localhost:8010/horas/sumar/2/1.5 | `{"operacion":"2.0 + 1.5","valor":3.5,"puerto":8010}` |
| http://localhost:8011/horas/faltante/6.5/20 | `"valor":13.5,"puerto":8011` |
| http://localhost:8010/horas/promedio/6/4 | `"valor":1.5` |
| http://localhost:8010/horas/minutos/2.5 | `"valor":150.0` |

### 3. Consumidor con Feign + balanceo de carga
Abrir http://localhost:8020/estadisticas/sumar/2/1.5 y recargar unas 4 veces.

✅ `Estadisticas: 2.0 + 1.5 = 3.5 h (atendido por puerto 8011)` y el puerto **alterna 8010 ↔ 8011**.

Esto demuestra que Feign encuentra el servicio **por su nombre** a través de Eureka y que reparte la carga entre las instancias.

### 4. Resumen (lo que usa la app)
Abrir http://localhost:8020/estadisticas/resumen?horas=2,1.5,3,1&meta=20

✅ `"disponible":true`, `"sesiones":4`, `"totalHoras":7.5`, `"totalMinutos":450.0`, `"promedio":1.88`,
`"faltante":12.5`, y `atendidoPor` con 8010 y 8011 mezclados.

### 5. Actuator
- http://localhost:8010/actuator/health → `{"status":"UP"}`
- http://localhost:8020/actuator/info → nombre del servicio y código del alumno

### 6. Tolerancia a fallos (circuit breaker)
1. **Ctrl+C solo en la terminal de 8011** y esperar unos 30 s.
   - En http://localhost:8761 queda 1 instancia de SERVICIO-HORAS.
   - `/estadisticas/sumar/2/1.5` responde **siempre con 8010**, sin error.
2. **Ctrl+C también en 8010** y recargar `/estadisticas/resumen?horas=2,1.5&meta=20` unas 5 veces:
   - Primeras veces: `"disponible":false, "mensaje":"servicio-horas no disponible: RetryableException"` → el **fallback**.
   - Tras unas 3: `...CallNotPermittedException` → el **circuito está abierto** y ya no intenta llamar al servicio caído.
3. Volver a levantar las 2 instancias, esperar unos 30 s y recargar → vuelve `"disponible":true`.

## Parte 2: App Android (con los 4 servicios arriba)

### 7. Login
Entrar con correo y contraseña, y luego con Google.

✅ Se llega a "Mis sesiones de estudio". Si Google da **error 10**, falta el SHA-1 de esta PC en Firebase.

### 8. El Lab 6 sigue funcionando
Crear, editar y eliminar una sesión; la lista se actualiza al instante.
Dejar unas 3 sesiones de **dos cursos distintos** para las siguientes pruebas.

### 9. Llamada a los microservicios
Ir a **Ver gráficos**, dejar la meta en 20 y tocar **Calcular**.

✅ Aparece "Sesiones: N, Total: X h (Y min), Promedio…, Te faltan Z h…, puertos: [8010, 8011, …]".
El total debe ser la suma de las horas de las sesiones.

### 10. Filtro por curso
Elegir un curso en el spinner y tocar **Calcular**.

✅ Los números cambian y cuentan **solo las sesiones de ese curso**.

### 11. Fallback desde la app
Detener las 2 instancias de servicio-horas y tocar **Calcular**.

✅ `⚠ servicio-horas no disponible: …` y la app **no se cae**.

### 12. Sin conexión al backend
Detener también servicio-estadisticas y tocar **Calcular**.

✅ "No se pudo conectar con servicio-estadisticas (http://10.0.2.2:8020/)…" y la app sigue funcionando.

## Si algo falla

| Prueba | Qué revisar |
|---|---|
| 1: no aparece un servicio | Que esa terminal no muestre error; esperar unos 30 s; que Eureka se haya levantado **primero** |
| 3: "Load balancer does not contain an instance" | servicio-horas todavía no está en Eureka (esperar) o el `name` del `@FeignClient` no coincide con `spring.application.name` |
| 3: siempre el mismo puerto | Solo hay 1 instancia arriba, o falta esperar a que estadisticas refresque la lista (5 s) |
| 9: "No se pudo conectar…" con todo arriba | Firewall de Windows bloqueando Java (permitirlo), o se usa un celular físico en vez del emulador (cambiar `BASE_URL` en `ApiClient.java` por la IP de la PC) |
| 9: "CLEARTEXT communication not permitted" | Falta `android:usesCleartextTraffic="true"` en el `AndroidManifest.xml` |
| 9: Total en 0 | No hay sesiones con ese filtro: elegir "Todos los cursos" |
