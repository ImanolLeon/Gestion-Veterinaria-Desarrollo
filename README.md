# Gestión Veterinaria — Backend

API REST de Spring Boot para una clínica veterinaria. Este documento describe los
endpoints **nuevos o con contrato modificado** introducidos en la ronda de trabajo de
autorización, integridad de citas y gestión de veterinarios. Para el resto de
endpoints (no listados aquí) el contrato no cambió.

Formato de error estándar de toda la API:

```json
{ "status": 400, "error": "Bad Request", "message": "...", "details": { } }
```

`details` solo aparece en errores de validación de campos.

## Cambio de contrato: estado de cita

El enum `Estado` de `Cita` es ahora `PENDIENTE, CONFIRMADA, EN_PROGRESO, COMPLETADA,
CANCELADA` (antes decía `EN_CURSO`; se renombró a `EN_PROGRESO` para alinear con el
frontend, que ya usaba ese nombre). Cualquier integración que enviara/esperara
`EN_CURSO` debe actualizarse a `EN_PROGRESO`.

## Autorización por endpoint (resumen)

Todas las reglas nuevas devuelven **403** (`{status:403, error:"Forbidden", message:"No
tiene permisos para realizar esta acción"}`) cuando el rol o el ownership no coincide.

| Endpoint | Regla |
|---|---|
| `GET /api/clientes/{id}/mascotas` | ADMIN, VETERINARIO, o CLIENTE dueño de `{id}` |
| `POST /api/mascotas` | ADMIN o CLIENTE (el `clienteId` del body se ignora para CLIENTE: se usa el del token) |
| `PUT/DELETE /api/mascotas/{id}` | ADMIN o CLIENTE dueño de la mascota |
| `GET /api/mascotas/{id}/historial` | ADMIN, VETERINARIO, o CLIENTE dueño de la mascota |
| `POST /api/historial` | ADMIN o VETERINARIO asignado a la cita (`dto.citaId`) |
| `PUT /api/historial/{id}` | ADMIN o VETERINARIO asignado a la cita del historial, y solo dentro de la ventana de edición (ver abajo) |
| `GET /api/citas` | ADMIN (todas) o VETERINARIO (solo las propias). CLIENTE usa `/api/citas/mis-citas` |
| `POST /api/citas` | ADMIN, VETERINARIO, o CLIENTE dueño de `mascotaId` |
| `PATCH /api/citas/{id}/estado` | ADMIN o VETERINARIO asignado a la cita |
| `PATCH /api/citas/{id}/reprogramar` | ADMIN o CLIENTE dueño de la cita |
| `PATCH /api/citas/{id}/pago` | ADMIN o VETERINARIO asignado a la cita |
| `DELETE /api/citas/{id}` | ADMIN, VETERINARIO asignado, o CLIENTE dueño |
| `PUT /api/clientes/{id}` | ADMIN o el propio cliente (el campo `dni` solo lo puede cambiar ADMIN; para CLIENTE se ignora) |
| `GET /api/reportes/citas-por-veterinario` | ADMIN (cualquier `veterinarioId`) o VETERINARIO (se fuerza su propio id, ignorando el query param) |
| `GET /api/reportes/servicios-populares` | Solo ADMIN |
| `PUT /api/veterinarios/{id}`, `PATCH /api/veterinarios/{id}/activo` | Solo ADMIN |
| `GET /api/veterinarios` | Todos los autenticados; CLIENTE solo ve veterinarios `activo:true` |
| `/api/veterinarios/{id}/horarios`, `/api/horarios/{id}` (CRUD) | Solo ADMIN |
| `/api/veterinarios/{id}/ausencias`, `/api/ausencias/{id}` (CRUD) | Solo ADMIN |

## Historial clínico — ventana de edición

`PUT /api/historial/{id}` solo se permite dentro de las **48 horas** posteriores a la
creación del historial (configurable vía `app.historial.edit-window-hours` en
`application.properties`). Fuera de esa ventana responde `400` con mensaje
descriptivo. Los historiales creados antes de este cambio (sin marca de tiempo de
creación) no tienen restricción.

## Citas — máquina de estados

`PATCH /api/citas/{id}/estado` valida transiciones:

```
PENDIENTE   -> CONFIRMADA | CANCELADA
CONFIRMADA  -> EN_PROGRESO | CANCELADA
EN_PROGRESO -> COMPLETADA
COMPLETADA, CANCELADA -> (terminales, ninguna transición válida)
```

Transición no permitida → `400` con mensaje `"Transición inválida de X a Y"`.

## Citas — cancelación lógica (campos nuevos)

`DELETE /api/citas/{id}` sigue respondiendo `204`, pero ahora además dentro de
`CitaResponseDto` quedan expuestos:

```json
{
  "...": "...",
  "canceladoPor": "CLIENTE" ,
  "fechaCancelacion": "2026-07-05T10:00:00"
}
```

`canceladoPor` es `CLIENTE | VETERINARIO | ADMIN` según quién canceló.

## Citas — disponibilidad consciente de duración

`GET /api/veterinarios/{id}/disponibilidad?fecha=YYYY-MM-DD&servicioId=` (nuevo
parámetro opcional `servicioId`). Si se envía, un slot se considera ocupado cuando
`[slot, slot + duracionMin_del_servicio)` se solapa con otra cita no cancelada. Sin
`servicioId` el comportamiento es el mismo de antes (solo mira el inicio exacto).

La disponibilidad también respeta ahora el horario laboral y las ausencias del
veterinario (ver más abajo); si no tiene horario configurado, usa el rango por
defecto 08:00–18:00 como antes.

## Citas — reprogramar

```
PATCH /api/citas/{id}/reprogramar
Body: { "fechaHora": "2026-07-10T09:30:00" }
```

Solo para citas en estado `PENDIENTE` o `CONFIRMADA`. Valida fecha futura (`400`) y
choque de horario del veterinario (`409`, excluyendo la propia cita). No cambia el
`estado` de la cita. Responde el `CitaResponseDto` actualizado.

## Citas — registro de pago

```
PATCH /api/citas/{id}/pago
Body: { "metodo": "EFECTIVO" | "TARJETA" | "YAPE_PLIN", "monto": 50.0 }
```

`CitaResponseDto` gana los campos `pagado` (boolean, default `false`), `metodoPago`,
`montoPago`.

## Veterinarios — gestión ADMIN

```
PUT /api/veterinarios/{id}
Body: { "nombres", "apellidos", "especialidad", "colegiatura" }
```

```
PATCH /api/veterinarios/{id}/activo
Body: { "activo": false }
Respuesta: {
  "veterinario": { ...VeterinarioResponseDto, "activo": false },
  "citasAfectadas": [ ...CitaResponseDto de sus citas futuras PENDIENTE/CONFIRMADA ]
}
```

Al desactivar un veterinario: su usuario queda deshabilitado (no puede loguearse), deja
de aparecer en `GET /api/veterinarios` para CLIENTE, y `POST /api/citas` rechaza con
`400` cualquier intento de agendarle una cita nueva. Sus citas futuras **no** se
cancelan automáticamente — se listan en `citasAfectadas` para que el ADMIN las
reprograme o reasigne manualmente. `VeterinarioResponseDto` gana el campo `activo`.

## Veterinarios — horario laboral y ausencias (nuevo, CRUD ADMIN)

```
GET/POST   /api/veterinarios/{veterinarioId}/horarios
PUT/DELETE /api/horarios/{id}
Body (POST/PUT): { "diaSemana": "MONDAY", "horaInicio": "08:00:00", "horaFin": "13:00:00" }
```

```
GET/POST   /api/veterinarios/{veterinarioId}/ausencias
PUT/DELETE /api/ausencias/{id}
Body (POST/PUT): { "fechaInicio": "2026-08-01", "fechaFin": "2026-08-05", "motivo": "Vacaciones" }
```

Si una fecha cae dentro de una ausencia, `GET .../disponibilidad` devuelve
`slotsDisponibles: []` para ese día.

## Notificaciones

No hay infraestructura de correo (`spring-boot-starter-mail` no está en el proyecto).
Se agregó una interfaz `NotificacionService` con una implementación que registra en el
log (`LogNotificacionService`), lista para reemplazar por una implementación SMTP real.
Se dispara al crear una cita, al cambiar a `CONFIRMADA`/`CANCELADA`, y en un job
programado que envía un recordatorio ~24h antes de citas `CONFIRMADA` (no repite el
recordatorio dos veces).

## Mascota — campos nuevos (aditivos)

`MascotaRequestDto`/`MascotaResponseDto` ganan:

```json
{ "sexo": "MACHO" | "HEMBRA" | "DESCONOCIDO", "esterilizado": false }
```

Ambos opcionales en el request; si se omiten, se guardan como `DESCONOCIDO` y `false`
respectivamente.

## Historial clínico — campo nuevo (aditivo)

`HistorialRequestDto`, `HistorialUpdateDto` y `HistorialResponseDto` ganan `pesoKg`
(`Double`, opcional) — peso de la mascota registrado en esa visita.
