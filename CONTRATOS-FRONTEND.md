# Contratos backend — estado actual (post-refactor)

Documento literal para el frontend Angular. Todos los nombres de campos y valores de
enum están copiados directamente de los DTOs/entidades Java, no parafraseados.

Formato de error estándar de toda la API:

```json
{ "status": 400, "error": "Bad Request", "message": "...", "details": { "campo": "mensaje" } }
```

`details` solo aparece en errores de validación de campos (`400` por `@Valid` fallido).
En el resto de errores (`ResponseStatusException`, `403`, `401`, etc.) `details` no está
presente en el JSON.

---

## 1. `CitaResponseDto` — todos los campos

```java
public record CitaResponseDto(
    Long id,
    LocalDateTime fechaHora,
    Estado estado,
    String notas,
    Long mascotaId,
    String mascotaNombre,
    Long veterinarioId,
    String veterinarioNombres,
    Long servicioId,
    String servicioNombre,
    double servicioPrecio,
    CanceladoPor canceladoPor,
    LocalDateTime fechaCancelacion,
    boolean pagado,
    MetodoPago metodoPago,
    Double montoPago)
```

Ejemplo real (cita pendiente, sin pagar, sin cancelar):

```json
{
  "id": 1,
  "fechaHora": "2026-07-20T11:30:00",
  "estado": "PENDIENTE",
  "notas": "Revisión general",
  "mascotaId": 1,
  "mascotaNombre": "Firulais",
  "veterinarioId": 2,
  "veterinarioNombres": "Ana Torres",
  "servicioId": 3,
  "servicioNombre": "Consulta general",
  "servicioPrecio": 50.0,
  "canceladoPor": null,
  "fechaCancelacion": null,
  "pagado": false,
  "metodoPago": null,
  "montoPago": null
}
```

Ejemplo real (cita cancelada por el cliente):

```json
{
  "id": 5,
  "fechaHora": "2026-07-21T09:00:00",
  "estado": "CANCELADA",
  "notas": null,
  "mascotaId": 4,
  "mascotaNombre": "Michi",
  "veterinarioId": 2,
  "veterinarioNombres": "Ana Torres",
  "servicioId": 1,
  "servicioNombre": "Vacunación",
  "servicioPrecio": 30.0,
  "canceladoPor": "CLIENTE",
  "fechaCancelacion": "2026-07-05T14:22:10",
  "pagado": false,
  "metodoPago": null,
  "montoPago": null
}
```

Ejemplo real (cita completada y pagada):

```json
{
  "id": 8,
  "fechaHora": "2026-07-01T10:00:00",
  "estado": "COMPLETADA",
  "notas": null,
  "mascotaId": 1,
  "mascotaNombre": "Firulais",
  "veterinarioId": 2,
  "veterinarioNombres": "Ana Torres",
  "servicioId": 3,
  "servicioNombre": "Consulta general",
  "servicioPrecio": 50.0,
  "canceladoPor": null,
  "fechaCancelacion": null,
  "pagado": true,
  "metodoPago": "YAPE_PLIN",
  "montoPago": 50.0
}
```

`CitaRequestDto` (body de `POST /api/citas`):

```json
{
  "mascotaId": 1,
  "veterinarioId": 2,
  "servicioId": 3,
  "fechaHora": "2026-07-20T11:30:00",
  "notas": "Revisión general"
}
```

Validaciones: `mascotaId`, `veterinarioId`, `servicioId`, `fechaHora` son `@NotNull`;
`fechaHora` además `@Future`. `notas` es opcional (`String`, sin `@NotBlank`).

---

## 2. `PATCH /api/citas/{id}/reprogramar`

- **Ruta**: `PATCH /api/citas/{id}/reprogramar`
- **Rol**: `ADMIN` o `CLIENTE` dueño de la cita (`@citaSecurityService.isOwnerOfCita`).
  El propio veterinario asignado **no** puede reprogramar (solo ADMIN/CLIENTE dueño).
- **Body** (`ReprogramarCitaDto`):
  ```json
  { "fechaHora": "2026-07-25T10:00:00" }
  ```
  `fechaHora`: `@NotNull @Future`.
- **Estados permitidos de la cita**: solo `PENDIENTE` o `CONFIRMADA`. No cambia el
  `estado` de la cita, solo `fechaHora`.
- **Respuesta**: `200 OK` con el `CitaResponseDto` actualizado.
- **Errores**:
  - `400 Bad Request` — `"Solo se pueden reprogramar citas PENDIENTE o CONFIRMADA"` si
    el estado actual no es uno de esos dos.
  - `400 Bad Request` — `"La fecha y hora de la cita deben ser futuras"` si `fechaHora`
    no es futura (aunque `@Future` ya debería rechazarlo antes con un 400 de validación
    de `MethodArgumentNotValidException`).
  - `409 Conflict` — `"El veterinario ya tiene una cita en ese horario"` si la nueva
    franja choca con otra cita del mismo veterinario (excluyendo la propia cita que se
    reprograma).
  - `404 Not Found` — `"Cita no encontrada"`.
  - `403 Forbidden` — `"No tiene permisos para realizar esta acción"` si no es ADMIN ni
    el cliente dueño.

---

## 3. `PATCH /api/citas/{id}/pago`

- **Ruta**: `PATCH /api/citas/{id}/pago`
- **Rol**: `ADMIN` o `VETERINARIO` asignado a la cita
  (`@citaSecurityService.isAssignedVet`).
- **Body** (`PagoRequestDto`):
  ```json
  { "metodo": "EFECTIVO", "monto": 50.0 }
  ```
  - `metodo`: `@NotNull`, enum `MetodoPago` — valores exactos: `"EFECTIVO"`,
    `"TARJETA"`, `"YAPE_PLIN"`.
  - `monto`: `@NotNull @Positive Double`.
- **Efecto**: fija `pagado = true`, `metodoPago = dto.metodo()`, `montoPago = dto.monto()`,
  `fechaPago = now()` (este último campo no se expone en `CitaResponseDto`).
- **Respuesta**: `200 OK` con el `CitaResponseDto` actualizado (`pagado: true`,
  `metodoPago`, `montoPago` reflejados).
- **Errores**:
  - `404 Not Found` — `"Cita no encontrada"`.
  - `403 Forbidden` — `"No tiene permisos para realizar esta acción"`.
  - `400 Bad Request` con `details` si `metodo` o `monto` faltan/son inválidos (p. ej.
    `monto` ≤ 0).

No hay restricción de estado de la cita para registrar pago (se puede pagar en
cualquier estado, incluida una cita ya `CANCELADA` — no hay chequeo de eso en el
código actual).

---

## 4. Transiciones válidas de `Estado` (`PATCH /api/citas/{id}/estado`)

Enum `Estado` completo: `PENDIENTE, CONFIRMADA, EN_PROGRESO, COMPLETADA, CANCELADA`
(nota: **no** es `EN_CURSO`, se renombró a `EN_PROGRESO`).

Tabla de transiciones permitidas (`CitaService.TRANSICIONES_VALIDAS`):

| Estado actual | Puede pasar a |
|---|---|
| `PENDIENTE` | `CONFIRMADA`, `CANCELADA` |
| `CONFIRMADA` | `EN_PROGRESO`, `CANCELADA` |
| `EN_PROGRESO` | `COMPLETADA` |
| `COMPLETADA` | (ninguna, terminal) |
| `CANCELADA` | (ninguna, terminal) |

- **Ruta**: `PATCH /api/citas/{id}/estado`
- **Rol**: `ADMIN` o `VETERINARIO` asignado a la cita. `CLIENTE` no tiene acceso a este
  endpoint (cancela vía `DELETE /api/citas/{id}`).
- **Body** (`EstadoUpdateDto`): `{ "estado": "CONFIRMADA" }` — `@NotNull`.
- **Respuesta**: `200 OK` con `CitaResponseDto` actualizado.
- **Efectos colaterales**:
  - Al llegar a `COMPLETADA`, si no existe ya un `HistorialClinico` para esa cita, se
    crea uno automáticamente con `diagnostico/tratamiento/observaciones = ""` (string
    vacío, no `null`) y `pesoKg = null`.
  - Al pasar a `CONFIRMADA` o `CANCELADA` se dispara `NotificacionService` (solo log, no
    hay email real).
- **Error de transición inválida**: `400 Bad Request`, mensaje exacto:
  `"Transición inválida de X a Y"` (p. ej. `"Transición inválida de COMPLETADA a
  PENDIENTE"`).
- Otros errores: `404 Not Found` — `"Cita no encontrada"`; `403 Forbidden` si el rol/
  ownership no coincide.

---

## 5. `DELETE /api/citas/{id}` — cancelación lógica

**Ya no es un borrado físico.** El registro de la cita permanece en la BD.

- **Rol**: `ADMIN`, `VETERINARIO` asignado, o `CLIENTE` dueño de la cita.
- **Efecto**: `estado = CANCELADA`, `canceladoPor` se fija según el rol de quien llama
  (`ADMIN` → `"ADMIN"`, `VETERINARIO` → `"VETERINARIO"`, `CLIENTE` → `"CLIENTE"`),
  `fechaCancelacion = now()`. Dispara notificación de cambio a `CANCELADA`.
- **Status HTTP de la respuesta**: `204 No Content` (sin body), igual que antes — el
  frontend no nota diferencia salvo por los campos nuevos que ahora trae
  `CitaResponseDto` en llamadas posteriores de lectura.
- **Errores**:
  - `400 Bad Request` — `"No se puede cancelar una cita completada"` si `estado ==
    COMPLETADA`.
  - `404 Not Found` — `"Cita no encontrada"`.
  - `403 Forbidden` si no hay ownership/rol adecuado.
- Una cita ya `CANCELADA` puede volver a recibir `DELETE` sin error (no hay chequeo
  específico para ese caso; simplemente vuelve a fijar `canceladoPor`/`fechaCancelacion`
  con los valores del nuevo llamante).

---

## 6. `GET /api/citas` por rol

- **Rol permitido**: solo `ADMIN` y `VETERINARIO`. `CLIENTE` recibe `403 Forbidden` en
  este endpoint — debe usar `GET /api/citas/mis-citas` en su lugar.
- **ADMIN**: devuelve `List<CitaResponseDto>` con **todas** las citas del sistema
  (`citaService.findAll()`).
- **VETERINARIO**: devuelve solo las citas donde `veterinarioId` corresponde al
  veterinario autenticado (resuelto internamente por `username` vía
  `citaSecurityService.resolveVeterinarioId`, ignorando cualquier parámetro). Si el
  usuario `VETERINARIO` autenticado no tiene un perfil de `Veterinario` asociado →
  `404 Not Found` — `"Perfil de veterinario no encontrado"`.
- No acepta query params de filtrado en ninguno de los dos casos.

---

## 7. Veterinarios: `activo`, `PUT`, `PATCH activo`, `GET` por rol

`VeterinarioResponseDto`:

```java
public record VeterinarioResponseDto(
    Long id, String nombres, String apellidos, String especialidad,
    String colegiatura, String email, String username, boolean activo)
```

```json
{
  "id": 2,
  "nombres": "Ana",
  "apellidos": "Torres",
  "especialidad": "Cirugía",
  "colegiatura": "CMVP-1234",
  "email": "ana.torres@vetcare.com",
  "username": "ana.torres",
  "activo": true
}
```

### `PUT /api/veterinarios/{id}` (ADMIN-only)

Body (`VeterinarioUpdateDto`, todos `@NotBlank`):

```json
{
  "nombres": "Ana",
  "apellidos": "Torres",
  "especialidad": "Cirugía",
  "colegiatura": "CMVP-1234"
}
```

No toca `username`/`email`/`password` (son de `Usuario`, gestión de auth aparte).
Respuesta: `200 OK` con `VeterinarioResponseDto`. Error: `404 Not Found` — `"Veterinario
no encontrado"`.

### `PATCH /api/veterinarios/{id}/activo` (ADMIN-only)

Body (`ActivoUpdateDto`, `@NotNull Boolean`):

```json
{ "activo": false }
```

Respuesta (`VeterinarioActivoResponseDto`):

```json
{
  "veterinario": { "id": 2, "nombres": "Ana", "...": "...", "activo": false },
  "citasAfectadas": [
    { "id": 12, "fechaHora": "2026-08-01T09:00:00", "estado": "CONFIRMADA", "...": "..." }
  ]
}
```

- Al desactivar (`activo:false`): también deshabilita el login (`usuario.isEnabled =
  false` y `usuario.activo = false`), y `citasAfectadas` lista las citas **futuras**
  (`fechaHora` posterior a ahora) en estado `PENDIENTE` o `CONFIRMADA` de ese
  veterinario — **no se cancelan automáticamente**, el ADMIN debe reprogramarlas/
  reasignarlas manualmente.
- Al reactivar (`activo:true`): revierte ambos flags a `true`; `citasAfectadas` viene
  como `[]` siempre en este caso (solo se calcula al desactivar).
- Error: `404 Not Found` — `"Veterinario no encontrado"`.

### `GET /api/veterinarios` por rol

- `ADMIN` y `VETERINARIO`: ven **todos** los veterinarios, incluidos `activo:false`.
- `CLIENTE`: solo ve veterinarios con `activo:true` (filtrado en memoria por
  `soloActivos`).
- No requiere rol específico para acceder (cualquier autenticado puede llamarlo; el
  filtrado depende del rol).

### `POST /api/citas` con veterinario inactivo

Si `veterinario.activo == false`, `POST /api/citas` responde `400 Bad Request` —
`"El veterinario no está activo"`, antes de siquiera chequear el servicio o el choque
de horario.

---

## 8. CRUD de horarios y ausencias de veterinario

Ambos controllers son **ADMIN-only** a nivel de clase (`@PreAuthorize("hasRole('ADMIN')")`
en el controller completo, no por método).

### Horarios — `HorarioVeterinarioRequestDto`/`ResponseDto`

```
GET    /api/veterinarios/{veterinarioId}/horarios   → List<HorarioVeterinarioResponseDto>
POST   /api/veterinarios/{veterinarioId}/horarios   → 201, HorarioVeterinarioResponseDto
PUT    /api/horarios/{id}                            → 200, HorarioVeterinarioResponseDto
DELETE /api/horarios/{id}                            → 204
```

Body (POST/PUT), `HorarioVeterinarioRequestDto`:

```json
{ "diaSemana": "MONDAY", "horaInicio": "08:00:00", "horaFin": "13:00:00" }
```

- `diaSemana`: `@NotNull`, enum Java `DayOfWeek` — valores exactos en **inglés y
  mayúsculas**: `MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY`.
- `horaInicio`/`horaFin`: `@NotNull LocalTime`, formato `"HH:mm:ss"` (acepta también
  `"HH:mm"` por el parser por defecto de Jackson/`tools.jackson` para `LocalTime`).

Respuesta (`HorarioVeterinarioResponseDto`):

```json
{ "id": 1, "veterinarioId": 2, "diaSemana": "MONDAY", "horaInicio": "08:00:00", "horaFin": "13:00:00" }
```

Errores: `404 Not Found` — `"Veterinario no encontrado"` (POST) / `"Horario no
encontrado"` (PUT/DELETE). **No hay validación** de que `horaInicio < horaFin` ni de
solapamiento entre horarios del mismo día — se acepta cualquier combinación.

### Ausencias — `AusenciaVeterinarioRequestDto`/`ResponseDto`

```
GET    /api/veterinarios/{veterinarioId}/ausencias   → List<AusenciaVeterinarioResponseDto>
POST   /api/veterinarios/{veterinarioId}/ausencias   → 201, AusenciaVeterinarioResponseDto
PUT    /api/ausencias/{id}                            → 200, AusenciaVeterinarioResponseDto
DELETE /api/ausencias/{id}                            → 204
```

Body (POST/PUT), `AusenciaVeterinarioRequestDto`:

```json
{ "fechaInicio": "2026-08-01", "fechaFin": "2026-08-05", "motivo": "Vacaciones" }
```

- `fechaInicio`/`fechaFin`: `@NotNull LocalDate`, formato `"YYYY-MM-DD"`.
- `motivo`: `String` opcional (sin anotación de validación), puede omitirse o ser
  `null`.

Respuesta (`AusenciaVeterinarioResponseDto`):

```json
{ "id": 1, "veterinarioId": 2, "fechaInicio": "2026-08-01", "fechaFin": "2026-08-05", "motivo": "Vacaciones" }
```

Errores:
- `400 Bad Request` — `"La fecha de fin no puede ser anterior a la fecha de inicio"` si
  `fechaFin < fechaInicio` (tanto en create como en update).
- `404 Not Found` — `"Veterinario no encontrado"` (POST) / `"Ausencia no encontrada"`
  (PUT/DELETE).

---

## 9. `GET /api/veterinarios/{id}/disponibilidad`

- **Query params**:
  - `fecha` (obligatorio, `@RequestParam`, formato ISO `YYYY-MM-DD`).
  - `servicioId` (opcional, `Long`). Si no se envía, el chequeo de choque solo mira si
    el slot coincide con el **inicio exacto** de otra cita (comportamiento legado). Si
    se envía, un slot se considera ocupado si `[slot, slot + duracionMin_del_servicio)`
    se solapa con `[citaInicio, citaInicio + duracionMin_del_servicio_de_esa_cita)` de
    cualquier cita existente ese día.
- **Respuesta** (`DisponibilidadResponseDto`):
  ```json
  {
    "veterinarioId": 2,
    "fecha": "2026-07-20",
    "slotsDisponibles": ["08:00:00", "08:30:00", "09:30:00", "10:00:00"]
  }
  ```
  `slotsDisponibles` es `List<LocalTime>`, slots cada 30 minutos.
- **Horario laboral usado**: si el veterinario tiene un `HorarioVeterinario` configurado
  para ese `DayOfWeek`, se usa su `horaInicio`/`horaFin`; si no tiene ninguno, se usa el
  rango por defecto **08:00–18:00**.
- **Ausencias**: si `fecha` cae dentro de un rango `[fechaInicio, fechaFin]` de alguna
  `AusenciaVeterinario` del veterinario, la respuesta trae `slotsDisponibles: []`
  directamente (no se evalúan horarios ni citas).
- **Errores**: `404 Not Found` — `"Veterinario no encontrado"` (id inválido) o
  `"Servicio no encontrado"` (si `servicioId` no existe).
- No requiere rol específico (accesible a cualquier autenticado).

---

## 10. Mascota — `MascotaRequestDto`/`MascotaResponseDto`

```java
public record MascotaRequestDto(
    @NotNull Long clienteId, @NotBlank String nombre, @NotBlank String especie,
    String raza, LocalDate fechaNacimiento, @Positive double pesoKg,
    Sexo sexo, Boolean esterilizado)
```

Enum `Sexo` — valores exactos: `"MACHO"`, `"HEMBRA"`, `"DESCONOCIDO"`.

- `clienteId`: `@NotNull`, pero **se ignora si quien llama es CLIENTE** en
  `POST /api/mascotas` (se resuelve el `clienteId` real desde el token). Solo lo respeta
  tal cual si quien llama es `ADMIN`.
- `nombre`, `especie`: `@NotBlank` (obligatorios).
- `raza`, `fechaNacimiento`: opcionales (sin anotación de validación, pueden ser
  `null`).
- `pesoKg`: `@Positive double` (primitivo, no puede ser `null`; debe ser > 0).
- `sexo`: **opcional** (`Sexo`, sin `@NotNull`). Si se omite o es `null`, se guarda como
  `"DESCONOCIDO"`.
- `esterilizado`: **opcional** (`Boolean`, sin `@NotNull`). Si se omite o es `null`, se
  guarda como `false`.

Body de ejemplo (`POST`/`PUT /api/mascotas`):

```json
{
  "clienteId": 1,
  "nombre": "Firulais",
  "especie": "Perro",
  "raza": "Labrador",
  "fechaNacimiento": "2020-01-01",
  "pesoKg": 10.0,
  "sexo": "MACHO",
  "esterilizado": false
}
```

```java
public record MascotaResponseDto(
    Long id, String nombre, String especie, String raza, LocalDate fechaNacimiento,
    double pesoKg, Long clienteId, String clienteNombres, Sexo sexo, boolean esterilizado)
```

```json
{
  "id": 1,
  "nombre": "Firulais",
  "especie": "Perro",
  "raza": "Labrador",
  "fechaNacimiento": "2020-01-01",
  "pesoKg": 10.0,
  "clienteId": 1,
  "clienteNombres": "Juan Pérez",
  "sexo": "MACHO",
  "esterilizado": false
}
```

`DELETE /api/mascotas/{id}` sigue siendo borrado lógico (`activo = false`), sin cambios
respecto a antes — no forma parte de este refactor.

---

## 11. Historial clínico — DTOs y ventana de edición

```java
public record HistorialRequestDto(
    @NotNull Long citaId, @NotNull Long mascotaId, String diagnostico,
    String tratamiento, String observaciones, Double pesoKg)

public record HistorialUpdateDto(
    String diagnostico, String tratamiento, String observaciones, Double pesoKg)

public record HistorialResponseDto(
    Long id, Long mascotaId, String mascotaNombre, Long citaId, String diagnostico,
    String tratamiento, String observaciones, LocalDate fecha, String veterinarioNombres,
    String servicioNombre, Double pesoKg)
```

- `pesoKg` es **opcional** (`Double`, nullable) en los tres DTOs — peso de la mascota
  registrado en esa visita. No confundir con `MascotaRequestDto.pesoKg` (ese es
  `double` primitivo y obligatorio; este es del historial puntual de la visita).
- `diagnostico`/`tratamiento`/`observaciones` no tienen `@NotBlank` en ninguno de los
  DTOs — pueden venir vacíos o `null` sin error de validación.

Body de ejemplo (`POST /api/historial`):

```json
{
  "citaId": 10,
  "mascotaId": 1,
  "diagnostico": "Otitis leve",
  "tratamiento": "Gotas óticas por 7 días",
  "observaciones": "Revisar en 2 semanas",
  "pesoKg": 10.5
}
```

Body de ejemplo (`PUT /api/historial/{id}`):

```json
{
  "diagnostico": "Otitis leve (actualizado)",
  "tratamiento": "Gotas óticas por 10 días",
  "observaciones": "Mejoría notable",
  "pesoKg": 10.7
}
```

Respuesta de ejemplo:

```json
{
  "id": 1,
  "mascotaId": 1,
  "mascotaNombre": "Firulais",
  "citaId": 10,
  "diagnostico": "Otitis leve",
  "tratamiento": "Gotas óticas por 7 días",
  "observaciones": "Revisar en 2 semanas",
  "fecha": "2026-07-05",
  "veterinarioNombres": "Ana Torres",
  "servicioNombre": "Consulta general",
  "pesoKg": 10.5
}
```

**Reglas de negocio en `POST /api/historial`** (no relacionadas con la ventana, pero
vigentes): solo se puede crear historial si `cita.estado == COMPLETADA`
(`400 Bad Request` — `"Solo se puede registrar historial para citas COMPLETADAS"` si
no), y solo una vez por cita (`409 Conflict` — `"Ya existe un historial para esta
cita"` si ya existe uno).

**Ventana de edición de `PUT /api/historial/{id}`**: 48 horas desde la creación del
historial (configurable vía `app.historial.edit-window-hours` en
`application.properties`, default `48`). Fuera de esa ventana:

- **Status**: `400 Bad Request`
- **Mensaje exacto**: `"La ventana de edición de 48h para este historial ha
  expirado"` (el número refleja el valor configurado, no siempre "48" literal si se
  cambia la propiedad).

Los historiales creados **antes** de este cambio (campo interno `creadoEn` en `null`
porque no existía la columna) **no tienen restricción** — se puede editar sin límite
de tiempo si `creadoEn == null`. `creadoEn` no se expone en `HistorialResponseDto`.

---

## 12. `PUT /api/clientes/{id}` con rol `CLIENTE` — comportamiento del `dni`

Body (`ClienteUpdateDto`):

```json
{
  "nombres": "Juan",
  "apellidos": "Pérez",
  "dni": "12345678",
  "telefono": "999888777",
  "direccion": "Av. Siempre Viva 123"
}
```

- `dni`: `@NotBlank @Size(min = 8, max = 15)` — **sigue siendo obligatorio en el body**
  (no se puede omitir ni mandar vacío, o falla la validación con `400` antes de llegar
  al service).
- **Si quien llama es `CLIENTE`**: el valor de `dni` que venga en el body **se ignora
  por completo** — el service conserva el `dni` actual de la entidad
  (`cliente.getDni()`), sin comparar ni rechazar. **No responde 400** por enviar un DNI
  distinto; simplemente no lo aplica.
- **Si quien llama es `ADMIN`**: el `dni` del body sí se aplica. Si el nuevo DNI ya está
  registrado en otro cliente → `409 Conflict` — `"El DNI ya está registrado"`.
- El resto de campos (`nombres`, `apellidos`, `telefono`, `direccion`) se actualizan
  igual sin importar el rol.

En resumen: un `CLIENTE` puede mandar cualquier valor de `dni` en el body (con tal de
que cumpla el formato `@Size(8,15)` para pasar la validación) y el backend simplemente
lo descarta silenciosamente, devolviendo el DNI real sin cambios en la respuesta.
