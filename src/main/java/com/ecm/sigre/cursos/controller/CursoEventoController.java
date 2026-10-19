package com.ecm.sigre.cursos.controller;

import com.ecm.sigre.cursos.dto.CursoEventoRequest;
import com.ecm.sigre.cursos.dto.CursoEventoResponse;
import com.ecm.sigre.cursos.model.EstadoCurso;
import com.ecm.sigre.cursos.service.CursoEventoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST de cursos/eventos, bajo la ruta {@code /api/cursos}.
 *
 * <p>Solo recibe y responde peticiones HTTP; la logica de negocio esta en
 * {@link CursoEventoService}.</p>
 */
@RestController
@RequestMapping("/api/cursos")
public class CursoEventoController {

    private final CursoEventoService servicio;

    /**
     * Crea el controlador con su servicio (inyeccion de dependencias por constructor).
     *
     * @param servicio servicio de cursos
     */
    public CursoEventoController(CursoEventoService servicio) {
        this.servicio = servicio;
    }

    /**
     * Crea un curso. Responde 201 con la ubicacion del nuevo recurso.
     *
     * @param solicitud datos validados del curso
     * @return curso creado
     */
    @PostMapping
    public ResponseEntity<CursoEventoResponse> crear(@Valid @RequestBody CursoEventoRequest solicitud) {
        CursoEventoResponse creado = servicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * Busca cursos con filtros opcionales por nombre, lugar, rango de fechas y estado.
     *
     * @param nombre texto contenido en el nombre (opcional)
     * @param lugar  texto contenido en el lugar (opcional)
     * @param desde  fecha inicial del rango, formato AAAA-MM-DD (opcional)
     * @param hasta  fecha final del rango, formato AAAA-MM-DD (opcional)
     * @param estado estado del curso (opcional)
     * @return lista de cursos
     */
    @GetMapping
    public List<CursoEventoResponse> buscar(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "lugar", required = false) String lugar,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "estado", required = false) EstadoCurso estado) {
        return servicio.buscar(nombre, lugar, desde, hasta, estado);
    }

    /**
     * Consulta el detalle de un curso.
     *
     * @param id identificador del curso
     * @return curso encontrado
     */
    @GetMapping("/{id}")
    public CursoEventoResponse obtenerPorId(@PathVariable("id") Long id) {
        return servicio.obtenerPorId(id);
    }

    /**
     * Actualiza un curso.
     *
     * @param id        identificador del curso
     * @param solicitud nuevos datos validados
     * @return curso actualizado
     */
    @PutMapping("/{id}")
    public CursoEventoResponse actualizar(@PathVariable("id") Long id,
                                          @Valid @RequestBody CursoEventoRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    /**
     * Cancela (deshabilita) un curso.
     *
     * @param id identificador del curso
     * @return curso con estado CANCELADO
     */
    @PatchMapping("/{id}/cancelar")
    public CursoEventoResponse cancelar(@PathVariable("id") Long id) {
        return servicio.cancelar(id);
    }

    /**
     * Elimina un curso que aun no ha iniciado. Responde 204 sin contenido.
     *
     * @param id identificador del curso
     * @return respuesta vacia con codigo 204
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
