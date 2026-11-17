# LAB6 - Sistema de Registro de Horas de Estudio

App Android (Java) con Firebase Authentication (Firebase UI), Cloud Firestore y MPAndroidChart.

## Pantallas (Nota 1)

| # | Pantalla | Clase |
|---|----------|-------|
| 1 | Inicio de sesión (Firebase UI + layout personalizado) | `MainActivity` + `activity_auth_picker.xml` |
| 2 | Lista de sesiones (tiempo real) | `HomeActivity` |
| 3 | Agregar / editar sesión | `SesionActivity` |
| 4 | Gráficos (barras por mes + torta por tipo, filtro por curso) | `ChartsActivity` |

## Estructura en Firestore

```
usuarios/{uid}/sesiones/{id}
    curso:          "Servicios y Aplicaciones para IoT"
    fecha:          Timestamp
    horasDedicadas: 2
    tipoActividad:  "Teoría" | "Práctica" | "Laboratorio"
```

Cada usuario solo ve sus sesiones porque la ruta usa su `uid` y las reglas (`firestore.rules`) lo exigen.

## Checklist en la consola de Firebase (proyecto lab6-20227163)

1. **Authentication > Sign-in method**: habilitar *Correo electrónico/contraseña* y *Google*.
2. **Authentication > Settings > User actions**: DESACTIVAR *Email enumeration protection*.
   Si está activo, Firebase UI no distingue entre "iniciar sesión" y "registrarse".
3. **SHA-1**: en Android Studio ejecutar `./gradlew signingReport` (o Gradle > app > Tasks > android > signingReport),
   copiar el SHA1 de `debug` y agregarlo en *Configuración del proyecto > Tus apps > Agregar huella digital*.
   Luego descargar de nuevo `google-services.json` y reemplazarlo en `app/`.
   Cada PC tiene su propia llave debug: si cambias de PC, agrega también su SHA-1 (si no, Google da error 10).
4. **Firestore Database > Reglas**: pegar el contenido de `firestore.rules` y publicar.
5. **Usuarios y permisos**: agregar `angie.alejandro@pucp.edu.pe` (rol Lector o Editor).
6. Repositorio GitHub `LAB6_20227163` en **público**.
