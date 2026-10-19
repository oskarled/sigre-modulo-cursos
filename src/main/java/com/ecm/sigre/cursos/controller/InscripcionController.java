package com.ecm.sigre.cursos.controller;

import com.ecm.sigre.cursos.dto.InscripcionRequest;
import com.ecm.sigre.cursos.dto.InscripcionResponse;
import com.ecm.sigre.cursos.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST de inscripciones, bajo la ruta {@code /api}.
 *
 * <p>Solo recibe y responde peticiones HTTP; la logica de negocio esta en
 * {@link InscripcionService}.</p>
 */
@RestController
@RequestMapping("/api")
public class InscripcionController {

    private final InscripcionService servicio;

    /**
     * Crea el controlador con su servicio (inyeccion de dependencias por constructor).
     *
     * @param servicio servicio de inscripciones
     */
    public InscripcionController(InscripcionService servicio) {
        this.servicio = servicio;
    }

    /**
     * Inscribe a un usuario en un curso. Responde 201 si hay cupo disponible.
     *
     * @param idCurso   identificador del curso
     * @param solicitud usuario que se inscribe
     * @return inscripcion registrada
     */
    @PostMapping("/cursos/{idCurso}/inscripciones")
    public ResponseEntity<InscripcionResponse> inscribir(@PathVariable("idCurso") Long idCurso,
                                                         @Valid @RequestBody InscripcionRequest solicitud) {
        InscripcionResponse creada = servicio.inscribir(idCurso, solicitud.idUsuario());
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/inscripciones/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    /**
     * Lista los asistentes inscritos en un curso.
     *
     * @param idCurso identificador del curso
     * @return inscripciones del curso
     */
    @GetMapping("/cursos/{idCurso}/inscripciones")
    public List<InscripcionResponse> listarPorCurso(@PathVariable("idCurso") Long idCurso) {
        return servicio.listarPorCurso(idCurso);
    }

    /**
     * Lista las inscripciones de un usuario (panel "Mis inscripciones").
     *
     * @param idUsuario identificador del usuario
     * @return inscripciones del usuario
     */
    @GetMapping("/usuarios/{idUsuario}/inscripciones")
    public List<InscripcionResponse> listarPorUsuario(@PathVariable("idUsuario") Long idUsuario) {
        return servicio.listarPorUsuario(idUsuario);
    }

    /**
     * Cancela una inscripcion y libera el cupo.
     *
     * @param idInscripcion identificador de la inscripcion
     * @return inscripcion cancelada
     */
    @PatchMapping("/inscripciones/{idInscripcion}/cancelar")
    public InscripcionResponse cancelar(@PathVariable("idInscripcion") Long idInscripcion) {
        return servicio.cancelar(idInscripcion);
    }
}
