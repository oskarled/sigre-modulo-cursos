package com.ecm.sigre.cursos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecm.sigre.common.exception.ReglaNegocioException;
import com.ecm.sigre.cursos.dto.InscripcionResponse;
import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.service.InscripcionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pruebas de la capa web de inscripciones: codigos HTTP del control de aforo y de la
 * cancelacion, sin base de datos (el servicio se simula).
 */
@WebMvcTest(InscripcionController.class)
class InscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InscripcionService servicio;

    /** Respuesta de ejemplo con id 7 y el estado indicado. */
    private InscripcionResponse construirRespuesta(EstadoInscripcion estado) {
        return new InscripcionResponse(7L, 3L, "Maria Perez", "maria.perez@correo.com", 10L,
                "Trabajo en Alturas", "Pista de entrenamiento", LocalDateTime.now().plusDays(10),
                LocalDateTime.now(), estado);
    }

    @Test
    void inscribirDebeResponder201ConUbicacion() throws Exception {
        when(servicio.inscribir(10L, 3L)).thenReturn(construirRespuesta(EstadoInscripcion.PENDIENTE));

        mockMvc.perform(post("/api/cursos/10/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuario\": 3}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/api/inscripciones/7")))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void inscribirSinCupoDebeResponder409() throws Exception {
        when(servicio.inscribir(10L, 3L)).thenThrow(new ReglaNegocioException(HttpStatus.CONFLICT,
                "No hay cupos disponibles: el curso alcanzo su aforo maximo"));

        mockMvc.perform(post("/api/cursos/10/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuario\": 3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No hay cupos disponibles: el curso alcanzo su aforo maximo"));
    }

    @Test
    void inscribirSinUsuarioDebeResponder400() throws Exception {
        mockMvc.perform(post("/api/cursos/10/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.idUsuario").exists());
    }

    @Test
    void cancelarDebeResponder200ConEstadoCancelado() throws Exception {
        when(servicio.cancelar(7L)).thenReturn(construirRespuesta(EstadoInscripcion.CANCELADO));

        mockMvc.perform(patch("/api/inscripciones/7/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void listarPorUsuarioDebeResponder200() throws Exception {
        when(servicio.listarPorUsuario(3L)).thenReturn(List.of(construirRespuesta(EstadoInscripcion.PENDIENTE)));

        mockMvc.perform(get("/api/usuarios/3/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCurso").value("Trabajo en Alturas"));
    }
}
