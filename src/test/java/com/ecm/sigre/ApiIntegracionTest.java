package com.ecm.sigre;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integracion de la API con la base H2 y los datos de demostracion.
 * Cada prueba usa un contexto limpio para no depender del orden.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegracionTest {

    @Autowired
    private MockMvc mvc;

    private static String json(String s) {
        return s.replace('\'', '"');
    }

    @Test
    void loginCorrectoDevuelveAutenticacionSatisfactoria() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'correo':'maria.perez@correo.com','contrasena':'Sigre2026'}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Autenticacion satisfactoria"))
                .andExpect(jsonPath("$.rol").value("ASISTENTE"));
    }

    @Test
    void loginInstructorIncluyeIdInstructor() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'correo':'carlos.ortega@ecm.com','contrasena':'Sigre2026'}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.idInstructor").value(1));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'correo':'maria.perez@correo.com','contrasena':'mala1234'}")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", containsString("Error en la autenticacion")));
    }

    @Test
    void registroValidaDatosYCorreoDuplicado() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'nombre':'Luis Diaz','correo':'luis@correo.com','contrasena':'Clave2026'}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ASISTENTE"))
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'nombre':'Luis Diaz','correo':'luis@correo.com','contrasena':'Clave2026'}")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'nombre':'','correo':'no-es-correo','contrasena':'corta'}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    void listarCursosCalculaCupos() throws Exception {
        mvc.perform(get("/api/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[?(@.nombreCurso=='Espacios Confinados')].aforoDisponible").value(contains(0)));
    }

    @Test
    void crudCurso() throws Exception {
        String inicio = LocalDateTime.now().plusDays(10).withNano(0).toString();
        String fin = LocalDateTime.now().plusDays(12).withNano(0).toString();
        String cuerpo = json("{'nombreCurso':'Rescate en Alturas','duracionHoras':20,'fechaInicio':'" + inicio
                + "','fechaFin':'" + fin + "','lugar':'Aula 101','cupoMaximo':15,'idInstructor':1}");
        mvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$.nombreInstructor").value("Carlos Ortega"));
        mvc.perform(put("/api/cursos/5").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo.replace("Aula 101", "Aula 102")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lugar").value("Aula 102"));
        mvc.perform(delete("/api/cursos/5")).andExpect(status().isNoContent());
        mvc.perform(get("/api/cursos/5")).andExpect(status().isNotFound());
    }

    @Test
    void crearCursoConFechaPasadaDevuelve400() throws Exception {
        mvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'nombreCurso':'X','duracionHoras':8,'fechaInicio':'2020-01-01T08:00:00',"
                                + "'fechaFin':'2020-01-02T08:00:00','lugar':'A','cupoMaximo':5,'idInstructor':1}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La fecha de inicio debe ser posterior a la fecha actual"));
    }

    @Test
    void reglasDeInscripcion() throws Exception {
        // Ana (6) se inscribe en el curso 1
        mvc.perform(post("/api/cursos/1/inscripciones").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'idUsuario':6}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        // duplicada
        mvc.perform(post("/api/cursos/1/inscripciones").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'idUsuario':6}")))
                .andExpect(status().isConflict());
        // curso 4 lleno
        mvc.perform(post("/api/cursos/4/inscripciones").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'idUsuario':3}")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El curso no tiene cupos disponibles"));
        // un instructor no se inscribe
        mvc.perform(post("/api/cursos/1/inscripciones").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'idUsuario':2}")))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelarYCambiarEstado() throws Exception {
        mvc.perform(patch("/api/inscripciones/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
        mvc.perform(patch("/api/inscripciones/1/cancelar"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/inscripciones/3/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'estado':'PAGADO'}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADO"));
        // no se aprueba un curso que no ha iniciado
        mvc.perform(patch("/api/inscripciones/3/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'estado':'APROBADO'}")))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/inscripciones/3/estado").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'estado':'INVENTADO'}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void certificados() throws Exception {
        mvc.perform(get("/api/certificados/verificar/ECM-2026-DEMO0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreParticipante").value("Maria Perez"));
        // inscripcion 4 ya tiene certificado, la 5 no esta aprobada
        mvc.perform(post("/api/inscripciones/4/certificado")).andExpect(status().isConflict());
        mvc.perform(post("/api/inscripciones/5/certificado")).andExpect(status().isConflict());
        mvc.perform(get("/api/usuarios/3/certificados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void crudInstructorYUsuario() throws Exception {
        mvc.perform(get("/api/instructores")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(post("/api/instructores").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'nombre':'Jorge Ramirez','correo':'jorge@ecm.com','contrasena':'Clave2026',"
                                + "'especialidad':'Primeros auxilios'}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
        mvc.perform(delete("/api/instructores/3")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/instructores/1")).andExpect(status().isConflict());
        mvc.perform(get("/api/usuarios").param("rol", "ASISTENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(delete("/api/usuarios/3")).andExpect(status().isConflict());
        mvc.perform(get("/api/usuarios/abc")).andExpect(status().isBadRequest());
    }
}
