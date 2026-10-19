package com.ecm.sigre.cursos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecm.sigre.common.exception.RecursoNoEncontradoException;
import com.ecm.sigre.common.exception.ReglaNegocioException;
import com.ecm.sigre.cursos.dto.CursoEventoRequest;
import com.ecm.sigre.cursos.dto.CursoEventoResponse;
import com.ecm.sigre.cursos.model.EstadoCurso;
import com.ecm.sigre.cursos.service.CursoEventoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Pruebas de la capa web de cursos: rutas, codigos HTTP y validaciones, sin base de datos
 * (el servicio se simula).
 */
@WebMvcTest(CursoEventoController.class)
class CursoEventoControllerTest {

    private static final String RUTA_BASE = "/api/cursos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CursoEventoService servicio;

    /** Solicitud valida con el cupo y la fecha de inicio indicados. */
    private CursoEventoRequest construirSolicitud(LocalDateTime inicio, int cupo) {
        return new CursoEventoRequest("Trabajo en Alturas", 40, inicio, inicio.plusDays(5),
                "Pista de entrenamiento", cupo, 1L);
    }

    /** Respuesta de ejemplo con id 10. */
    private CursoEventoResponse construirRespuesta() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        return new CursoEventoResponse(10L, "Trabajo en Alturas", 40, inicio, inicio.plusDays(5),
                "Pista de entrenamiento", 20, 5L, 15, EstadoCurso.PROGRAMADO, 1L, "Carlos Ortega");
    }

    @Test
    void postValidoDebeResponder201ConUbicacion() throws Exception {
        when(servicio.crear(any(CursoEventoRequest.class))).thenReturn(construirRespuesta());

        mockMvc.perform(post(RUTA_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                construirSolicitud(LocalDateTime.now().plusDays(10), 20))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/api/cursos/10")))
                .andExpect(jsonPath("$.aforoDisponible").value(15));
    }

    @Test
    void postConCupoCeroDebeResponder400() throws Exception {
        mockMvc.perform(post(RUTA_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                construirSolicitud(LocalDateTime.now().plusDays(10), 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cupoMaximo").exists());

        verify(servicio, never()).crear(any(CursoEventoRequest.class));
    }

    @Test
    void postConFechaDeInicioPasadaDebeResponder400() throws Exception {
        mockMvc.perform(post(RUTA_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                construirSolicitud(LocalDateTime.now().minusDays(1), 20))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaInicio").exists());
    }

    @Test
    void getListaDebeResponder200() throws Exception {
        when(servicio.buscar(null, null, null, null, null)).thenReturn(List.of(construirRespuesta()));

        mockMvc.perform(get(RUTA_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].aforoDisponible").value(15));
    }

    @Test
    void getPorIdInexistenteDebeResponder404() throws Exception {
        when(servicio.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe un curso con id 99"));

        mockMvc.perform(get(RUTA_BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un curso con id 99"));
    }

    @Test
    void putQueIncumpleUnaReglaDebeResponder409() throws Exception {
        when(servicio.actualizar(eq(10L), any(CursoEventoRequest.class)))
                .thenThrow(new ReglaNegocioException(HttpStatus.CONFLICT, "El cupo es menor a los inscritos"));

        mockMvc.perform(put(RUTA_BASE + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                construirSolicitud(LocalDateTime.now().plusDays(10), 3))))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteDebeResponder204() throws Exception {
        mockMvc.perform(delete(RUTA_BASE + "/10"))
                .andExpect(status().isNoContent());

        verify(servicio).eliminar(10L);
    }
}
