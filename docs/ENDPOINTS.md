# ENDPOINTS de la API SIGRE

**Proyecto:** SIGRE — Sistema de Registro para Eventos, E.C.M. Training Center S.A.S.
**Evidencia:** GA7-220501096-AA5-EV04 — API del proyecto
**Aprendiz:** Oscar Ledesma Puello · Ficha 3134533

- URL base local: `http://localhost:8080`
- Formato: JSON (`Content-Type: application/json`)
- Fechas: `AAAA-MM-DDTHH:mm:ss` (ejemplo `2026-11-20T08:00:00`)
- Documentación interactiva (Swagger): `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI: `http://localhost:8080/v3/api-docs`

## Códigos de respuesta

| Código | Significado |
|---|---|
| 200 | Consulta o actualización correcta |
| 201 | Registro creado |
| 204 | Registro eliminado (sin cuerpo) |
| 400 | Datos inválidos (el campo `errores` indica qué campo falló) |
| 401 | Error en la autenticación |
| 404 | El registro no existe |
| 409 | Regla de negocio incumplida (cupos, duplicados, fechas, estados) |

Los errores usan el formato ProblemDetail:

```json
{ "type": "about:blank", "title": "Regla de negocio", "status": 409,
  "detail": "El curso no tiene cupos disponibles", "instance": "/api/cursos/4/inscripciones" }
```

## 1. Autenticación — `/api/auth`

| Método | Endpoint | Descripción | Cuerpo |
|---|---|---|---|
| POST | `/api/auth/registro` | Registra un participante (rol ASISTENTE) | `{"nombre","correo","contrasena","documento?","telefono?"}` |
| POST | `/api/auth/login` | Inicia sesión. Responde "Autenticacion satisfactoria" o 401 | `{"correo","contrasena"}` |

## 2. Usuarios — `/api/usuarios`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Lista usuarios. Filtro opcional `?rol=ASISTENTE\|INSTRUCTOR\|ADMIN` |
| GET | `/api/usuarios/{id}` | Consulta un usuario |
| POST | `/api/usuarios` | Crea un usuario `{"nombre","correo","contrasena","documento?","telefono?","rol","activo?"}` |
| PUT | `/api/usuarios/{id}` | Actualiza un usuario (contraseña vacía = se conserva) |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario sin inscripciones (409 si tiene) |
| GET | `/api/usuarios/{id}/inscripciones` | Inscripciones del usuario |
| GET | `/api/usuarios/{id}/certificados` | Certificados del usuario |

## 3. Instructores — `/api/instructores`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/instructores` | Lista instructores |
| GET | `/api/instructores/{id}` | Consulta un instructor |
| POST | `/api/instructores` | Crea instructor y su usuario `{"nombre","correo","contrasena","telefono?","especialidad","licenciaSst?"}` |
| PUT | `/api/instructores/{id}` | Actualiza un instructor |
| DELETE | `/api/instructores/{id}` | Elimina un instructor sin cursos (409 si tiene) |
| GET | `/api/instructores/{id}/cursos` | Cursos asignados al instructor |

## 4. Cursos — `/api/cursos`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/cursos` | Lista cursos con cupos calculados. Filtro `?estado=PROGRAMADO\|EN_CURSO\|FINALIZADO\|CANCELADO` |
| GET | `/api/cursos/{id}` | Consulta un curso |
| POST | `/api/cursos` | Crea un curso `{"nombreCurso","duracionHoras","fechaInicio","fechaFin","lugar","cupoMaximo","idInstructor"}` |
| PUT | `/api/cursos/{id}` | Actualiza un curso |
| PATCH | `/api/cursos/{id}/estado` | Cambia el estado `{"estado":"EN_CURSO"}` |
| DELETE | `/api/cursos/{id}` | Elimina un curso sin inscripciones (409 si tiene) |
| GET | `/api/cursos/{id}/inscripciones` | Inscritos del curso |
| POST | `/api/cursos/{id}/inscripciones` | Inscribe un participante `{"idUsuario":3}` |

## 5. Inscripciones — `/api/inscripciones`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/inscripciones/{id}` | Consulta una inscripción |
| PATCH | `/api/inscripciones/{id}/cancelar` | Cancela (solo hasta 24 horas antes del inicio) |
| PATCH | `/api/inscripciones/{id}/estado` | Cambia el estado `{"estado":"PAGADO"}` |
| POST | `/api/inscripciones/{id}/certificado` | Emite el certificado (solo inscripciones APROBADAS) |

## 6. Certificados — `/api/certificados`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/certificados` | Lista certificados emitidos |
| GET | `/api/certificados/{id}` | Consulta un certificado |
| GET | `/api/certificados/verificar/{codigo}` | Verifica un certificado por su código (ej. `ECM-2026-DEMO0001`) |

**Total: 30 endpoints.**

## Reglas de negocio

- Un curso nuevo debe iniciar en el futuro y su fecha de fin debe ser posterior a la de inicio.
- Solo se inscriben usuarios ASISTENTE activos en cursos PROGRAMADOS que no hayan iniciado y tengan cupo.
- Un participante no puede inscribirse dos veces en el mismo curso.
- La cancelación libera el cupo y solo se permite hasta 24 horas antes del inicio.
- Estados del curso: PROGRAMADO → EN_CURSO → FINALIZADO, o CANCELADO.
- Estados de la inscripción: PENDIENTE → PAGADO → APROBADO | NO_APROBADO, o CANCELADO.
- El certificado solo se emite a inscripciones APROBADAS y una sola vez.
- Las contraseñas se guardan cifradas con BCrypt y nunca se devuelven.

## Datos de demostración (contraseña de todos: `Sigre2026`)

| id | Usuario | Correo | Rol |
|---:|---|---|---|
| 1 | Elkin Caicedo | elkin.caicedo@ecm.com | ADMIN |
| 2 | Carlos Ortega (instructor 1) | carlos.ortega@ecm.com | INSTRUCTOR |
| 3 | Maria Perez | maria.perez@correo.com | ASISTENTE |
| 4 | Pedro Martinez (instructor 2) | pedro.martinez@ecm.com | INSTRUCTOR |
| 5 | Juan Gomez | juan.gomez@correo.com | ASISTENTE |
| 6 | Ana Torres | ana.torres@correo.com | ASISTENTE |

| id | Curso | Estado | Observación |
|---:|---|---|---|
| 1 | Trabajo en Alturas Nivel Avanzado | PROGRAMADO | inicia en 20 días |
| 2 | Seguridad Industrial Básica | PROGRAMADO | inicia en 30 días |
| 3 | Primeros Auxilios | FINALIZADO | tiene un certificado emitido |
| 4 | Espacios Confinados | PROGRAMADO | cupo 2, lleno (para probar "sin cupos") |
