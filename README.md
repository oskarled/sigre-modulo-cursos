# SIGRE · Backend (`sigre-modulo-cursos`)

API REST del **Sistema de Registro para Eventos (SIGRE)** de E.C.M. Training Center S.A.S.
Proyecto formativo SENA · Tecnología en Análisis y Desarrollo de Software · ficha 3134533.
Aprendiz: Oscar Ledesma Puello.

| Capa | Tecnología |
|---|---|
| Backend (este repositorio) | Java 17 · Spring Boot 3.2 · Spring Data JPA · Bean Validation · springdoc (Swagger) |
| Base de datos | MySQL 8 (producción) · H2 en memoria (perfil `dev` y pruebas) |
| Frontend | React 18 + TypeScript + Vite (`sigre-frontend`) |

## Requisitos

- JDK 17 o superior
- Maven 3.9 (o un IDE que lo traiga: IntelliJ IDEA, NetBeans, VS Code con *Extension Pack for Java*)
- MySQL 8 solo si se usa el perfil por defecto

## Ejecutar

**Sin instalar MySQL (recomendado para las pruebas con Postman):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Usa H2 en memoria y carga los datos de demostración. Al reiniciar, los datos vuelven a su estado inicial.

**Con MySQL:**

```bash
# Windows (PowerShell)
$env:SIGRE_DB_PASSWORD="tu_clave"; mvn spring-boot:run
```

Crea la base `sigre_db` y sus tablas automáticamente. Variables opcionales: `SIGRE_DB_URL`, `SIGRE_DB_USER`,
`SIGRE_DB_PASSWORD`, `SIGRE_CORS_ORIGENES`, `PORT`.

La API queda en `http://localhost:8080`:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Consola H2 (perfil dev): http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:sigre`, usuario `sa`

## Pruebas

```bash
mvn test
```

Pruebas de integración con MockMvc sobre H2 (`src/test/java/com/ecm/sigre/ApiIntegracionTest.java`).

**Postman:** importe `postman/SIGRE-API.postman_collection.json` y use *Run collection*. Cada petición trae
sus pruebas automáticas (código de respuesta y contenido). Reinicie el backend antes de repetir la ejecución
completa para volver a los datos iniciales.

## Endpoints

Ver [`docs/ENDPOINTS.md`](docs/ENDPOINTS.md): 30 endpoints en 6 módulos (autenticación, usuarios, instructores,
cursos, inscripciones y certificados).

## Estructura

```
src/main/java/com/ecm/sigre/
  config/       CORS, BCrypt, Swagger y datos de demostración
  controller/   Controladores REST (una clase por recurso)
  dto/          Objetos de entrada (Request) y salida (Response) con validaciones
  exception/    Excepciones de negocio y manejador global (ProblemDetail)
  model/        Entidades JPA y enumeraciones de estados
  repository/   Repositorios Spring Data JPA
  service/      Lógica y reglas de negocio
```

## Conectar el frontend

En `sigre-frontend`, copie `.env.example` a `.env.local` con `VITE_API_URL=http://localhost:8080` y ejecute
`npm run dev`. El CORS ya permite `http://localhost:5173`.

## Flujo de ramas

`main` (estable) ← `develop` (integración) ← `feature/*` (una rama por funcionalidad). Los cambios entran a
`develop` por pull request y a `main` al cerrar cada sprint.
