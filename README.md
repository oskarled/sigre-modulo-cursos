# SIGRE - Módulo de cursos/eventos e inscripciones

**Evidencia:** GA7-220501096-AA3-EV01 - Codificación de módulos del software Stand alone, web y móvil de acuerdo al proyecto a desarrollar
**Aprendiz:** Oscar Ramón Ledesma Puello - Tecnólogo en Análisis y Desarrollo de Software (SENA), ficha 3134533
**Proyecto:** SIGRE (Sistema de Gestión de Registro para Eventos) - E.C.M. Training Center S.A.S.

## 1. Descripción

Módulo **backend web** del SIGRE, desarrollado con **Spring Boot**. Cubre la gestión de los cursos/eventos de la academia y la inscripción de los asistentes con **control de aforo**, que es la regla de seguridad más crítica del sistema (no se puede superar el cupo máximo de una pista de entrenamiento).

Ofrece una API REST con las cuatro operaciones del CRUD (**inserción, consulta, actualización y eliminación**) sobre cursos, y las operaciones de inscripción: inscribirse, cancelar, ver mis inscripciones y ver los asistentes de un curso.

El módulo se construyó a partir de los artefactos del proyecto:

| Artefacto | Qué se tomó de él |
|---|---|
| Diagrama de clases (GA4-220501095-AA2-EV04) | Clases `Rol`, `Usuario`, `Instructor`, `CursoEvento`, `Inscripcion`, sus relaciones y métodos (`validarLicencia`, `obtenerAforoDisponible`, `deshabilitar`, `cancelar`) |
| Modelo lógico y diccionario de datos (GA4-220501095-AA1-EV02) | Tablas, columnas, tipos, restricciones UNIQUE y llaves foráneas; bloqueo de filas para evitar la sobreventa de cupos |
| Arquitectura (GA4-220501095-AA2-EV05) | Arquitectura en capas y patrones: inyección de dependencias por constructor, Repository, DTO, Singleton (beans de Spring), manejador global de excepciones y bloqueo pesimista |
| Historias de usuario (GA2-220501093-AA1-EV03) | HU-05 a HU-11 con sus criterios de aceptación |
| Requisitos (GA1-220501092-AA4-EV01) | RF-01 a RF-05 y RF-10 a RF-13 |

## 2. Tecnologías

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 3.2 (Spring Web, Spring Data JPA, Bean Validation) |
| Lenguaje | Java 17 |
| Almacenamiento de datos | MySQL 8 (InnoDB) en producción; H2 en memoria para desarrollo y pruebas; acceso con JPA/Hibernate |
| Construcción y dependencias | Maven |
| Pruebas | JUnit 5, Mockito, MockMvc |
| Seguridad | BCrypt (`spring-security-crypto`) para el hash de las contraseñas de los datos de demostración |
| Control de versiones | Git (local) y GitHub (remoto), con Git Flow simplificado |

## 3. Estructura del proyecto

```
sigre-modulo-cursos/
├── pom.xml
├── README.md
├── docs/
│   ├── script_base_datos.sql      (script opcional de MySQL, según el diccionario de datos)
│   └── peticiones.http            (ejemplos de peticiones para probar la API)
└── src/
    ├── main/java/com/ecm/sigre/
    │   ├── SigreApplication.java
    │   ├── common/exception/     excepciones y manejador global
    │   ├── config/               carga de roles y datos de demostración
    │   ├── usuarios/             Rol, Usuario, Instructor (entidades de apoyo) y sus repositorios
    │   └── cursos/
    │       ├── controller/       CursoEventoController, InscripcionController
    │       ├── service/          CursoEventoService, InscripcionService (reglas de negocio)
    │       ├── repository/       CursoEventoRepository, InscripcionRepository
    │       ├── specification/    búsqueda dinámica de cursos
    │       ├── model/            CursoEvento, Inscripcion y sus estados
    │       └── dto/              objetos de entrada y salida de la API
    ├── main/resources/           application.properties, application-dev.properties
    └── test/java/...             pruebas de repositorios, servicios y controladores
```

Arquitectura en capas: **controller → service → repository → base de datos**. Las entidades nunca se exponen al cliente: la API usa DTO.

## 4. Requisitos

- JDK 17 o superior
- Maven 3.9 o superior
- MySQL 8 (solo para el perfil de producción; el perfil `dev` usa H2 y no requiere instalación)

## 5. Cómo ejecutar

### Opción A: H2 en memoria con datos de demostración (más rápida)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Se cargan los 4 roles y estos datos de demostración (contraseña de todos: `Sigre2026*`, guardada con BCrypt):

| id | Usuario | Rol |
|---|---|---|
| 1 | admin@ecm.com | ADMIN |
| 2 | instructor@ecm.com | INSTRUCTOR (instructor id = 1, licencia SST vigente) |
| 3 | maria.perez@correo.com | ASISTENTE |
| 4 | juan.gomez@correo.com | ASISTENTE |

La consola de H2 queda en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:sigre_dev`, usuario `sa`, sin contraseña).

### Opción B: MySQL

1. Tener MySQL en ejecución en el puerto 3306. La base `sigre_db` se crea sola (también se puede usar `docs/script_base_datos.sql`).
2. Definir las credenciales con variables de entorno (Git Bash). Con `SIGRE_DATOS_DEMO=true` se cargan los datos de demostración la primera vez:

```bash
export DB_USER=root
export DB_PASSWORD=su_contraseña
export SIGRE_DATOS_DEMO=true
mvn spring-boot:run
```

La API queda en `http://localhost:8080/api`.

### Ejecutar las pruebas

```bash
mvn test
```

## 6. Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/cursos` | Crear un curso (HU-05) | 201, 400, 404, 409 |
| GET | `/api/cursos` | Listar y buscar por `nombre`, `lugar`, `desde`, `hasta`, `estado` (HU-08) | 200 |
| GET | `/api/cursos/{id}` | Detalle de un curso con su aforo disponible | 200, 404 |
| PUT | `/api/cursos/{id}` | Editar un curso (HU-06) | 200, 400, 404, 409 |
| PATCH | `/api/cursos/{id}/cancelar` | Cancelar (deshabilitar) un curso | 200, 404 |
| DELETE | `/api/cursos/{id}` | Eliminar un curso que no ha iniciado (HU-07) | 204, 404, 409 |
| POST | `/api/cursos/{idCurso}/inscripciones` | Inscribirse a un curso con control de aforo (HU-09) | 201, 400, 404, 409 |
| GET | `/api/cursos/{idCurso}/inscripciones` | Ver los asistentes inscritos (RF-05) | 200, 404 |
| GET | `/api/usuarios/{idUsuario}/inscripciones` | Ver mis inscripciones (HU-11) | 200, 404 |
| PATCH | `/api/inscripciones/{id}/cancelar` | Cancelar una inscripción y liberar el cupo (HU-10) | 200, 404, 409 |

Ejemplo de cuerpo para crear o editar un curso:

```json
{
  "nombreCurso": "Trabajo en Alturas",
  "duracionHoras": 40,
  "fechaInicio": "2027-03-01T08:00:00",
  "fechaFin": "2027-03-05T17:00:00",
  "lugar": "Pista de entrenamiento E.C.M.",
  "cupoMaximo": 20,
  "idInstructor": 1
}
```

Más ejemplos listos para ejecutar en `docs/peticiones.http`.

### Reglas de negocio implementadas

- **Crear/editar curso:** nombre, lugar, fechas, cupo e instructor obligatorios; la fecha de inicio debe ser posterior a la actual (HU-05); la de fin, posterior a la de inicio; el cupo, un entero positivo; y el instructor debe tener la **licencia SST vigente**.
- **Editar:** solo cursos programados que no han iniciado; el cupo no puede quedar por debajo de los inscritos actuales (HU-06).
- **Eliminar:** solo cursos que no han iniciado; se eliminan en cascada sus inscripciones, por la composición del diagrama de clases (HU-07).
- **Buscar:** filtros opcionales por nombre, lugar, rango de fechas y estado; cada resultado incluye el cupo disponible (HU-08).
- **Inscribirse:** solo usuarios activos con rol ASISTENTE; el curso debe estar programado y no haber iniciado; no se permite inscribirse dos veces (una inscripción cancelada se reactiva); y se verifica el aforo bajo **bloqueo pesimista** de la fila del curso (`PESSIMISTIC_WRITE`, equivalente a `SELECT ... FOR UPDATE`), de modo que las solicitudes simultáneas no superen el cupo (HU-09).
- **Cancelar inscripción:** solo hasta 24 horas antes del inicio; el cupo se libera (HU-10).
- **Errores:** respuestas con formato `ProblemDetail` (RFC 7807), sin trazas internas del servidor.

### Alcance y trabajo posterior

Este módulo no incluye, por pertenecer a otros módulos del backlog: registro y login con JWT/2FA (HU-01 a HU-04), notificaciones por correo (HU-12 a HU-14), reportes PDF/Excel (HU-15, HU-16), certificados (HU-17 en adelante), asistencia con QR ni el frontend en React (AA4). Mientras el módulo de autenticación no exista, el asistente se identifica en la inscripción con su `idUsuario`; después se tomará del token JWT.

## 7. Estándares de codificación aplicados

| Elemento | Estándar | Ejemplo |
|---|---|---|
| Paquetes | Minúsculas, sin guiones bajos, organizados por módulo y por capa | `com.ecm.sigre.cursos.service` |
| Clases e interfaces | PascalCase, sustantivos; sufijo según su capa | `CursoEvento`, `InscripcionService`, `CursoEventoController` |
| Métodos | camelCase, empiezan con verbo | `inscribir`, `obtenerAforoDisponible`, `validarLicencia` |
| Variables y atributos | camelCase, nombres descriptivos | `cupoMaximo`, `fechaInicio` |
| Constantes | MAYUSCULAS_CON_GUION_BAJO | `HORAS_LIMITE_CANCELACION` |
| Enumeraciones | Tipo en PascalCase; valores en MAYUSCULAS | `EstadoInscripcion.PENDIENTE` |
| Tablas y columnas | snake_case, igual que el diccionario de datos | `curso_evento`, `cupo_maximo` |
| Rutas de la API | Minúsculas y sustantivos en plural | `/api/cursos` |
| Pruebas | Clase `NombreClasePrueba`; método que describe el comportamiento esperado | `inscribirDebeFallarCuandoElCursoAlcanzoSuAforoMaximo` |
| Commits | Convención `tipo: descripción` (feat, fix, test, docs, chore) | `feat: agrega control de aforo` |

**Comentarios:** todas las clases y métodos públicos tienen documentación Javadoc, y los pasos con lógica no evidente tienen comentarios en línea.
**Inyección de dependencias:** por constructor, con atributos `final` (no por campo).

## 8. Control de versiones (Git local y GitHub)

Se usa el flujo definido en el plan de trabajo: `main` (estable), `develop` (integración) y `feature/` (cada funcionalidad).

```bash
git init
git branch -M main
git add .gitignore pom.xml src/main/java/com/ecm/sigre/SigreApplication.java src/main/resources
git commit -m "chore: estructura inicial del proyecto Spring Boot"
git remote add origin https://github.com/oskarled/sigre-modulo-cursos.git
git push -u origin main

git switch -c develop
git push -u origin develop

git switch -c feature/cursos-inscripciones
git add src/main/java/com/ecm/sigre/usuarios src/main/java/com/ecm/sigre/common
git commit -m "feat: agrega entidades de usuarios y manejo global de excepciones"
git add src/main/java/com/ecm/sigre/cursos/model src/main/java/com/ecm/sigre/cursos/repository src/main/java/com/ecm/sigre/cursos/specification
git commit -m "feat: agrega entidades CursoEvento e Inscripcion y sus repositorios"
git add src/main/java/com/ecm/sigre/cursos/dto src/main/java/com/ecm/sigre/cursos/service
git commit -m "feat: agrega servicios con reglas de negocio y control de aforo"
git add src/main/java/com/ecm/sigre/cursos/controller src/main/java/com/ecm/sigre/config
git commit -m "feat: agrega controladores REST y carga de datos iniciales"
git add src/test
git commit -m "test: agrega pruebas de repositorios, servicios y controladores"
git add README.md docs
git commit -m "docs: agrega README, script SQL y ejemplos de peticiones"
git push -u origin feature/cursos-inscripciones
```

Después, en GitHub: abrir un Pull Request de `feature/cursos-inscripciones` hacia `develop`, fusionarlo y luego integrar `develop` en `main`.

## 9. Relación con la lista de chequeo (IE-GA7-220501096-AA3-EV01)

| Indicador | Dónde se cumple |
|---|---|
| Selecciona y aplica un framework para la codificación del proyecto | Spring Boot 3.2 (`pom.xml`, `SigreApplication`, controladores, servicios y repositorios) |
| Integra herramientas para almacenamiento de datos | MySQL y H2 con Spring Data JPA / Hibernate (`model`, `repository`, `application*.properties`, `docs/script_base_datos.sql`) |
| Utiliza un estándar de codificación definido y usa comentarios en el código | Sección 7 de este documento y Javadoc en todas las clases |
| Utiliza un estándar de codificación definido | Nombres de paquetes, clases, métodos, variables y constantes según la sección 7 |
