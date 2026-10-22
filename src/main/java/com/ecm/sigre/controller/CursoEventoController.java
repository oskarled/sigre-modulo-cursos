package com.ecm.sigre.controller;

import com.ecm.sigre.dto.CambioEstadoRequest;
import com.ecm.sigre.dto.CursoEventoRequest;
import com.ecm.sigre.dto.CursoEventoResponse;
import com.ecm.sigre.dto.InscripcionRequest;
import com.ecm.sigre.dto.InscripcionResponse;
import com.ecm.sigre.service.CursoEventoService;
import com.ecm.sigre.service.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CRUD de cursos y eventos, e inscripciones de cada curso. */
@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos", description = "Cursos y eventos de formacion")
public class CursoEventoController {

    private final CursoEventoService cursoService;
    private final InscripcionService inscripcionService;

    public CursoEventoController(CursoEventoService cursoService, InscripcionService inscripcionService) {
        this.cursoService = cursoService;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    @Operation(summary = "Listar cursos", description = "Filtro opcional ?estado=PROGRAMADO|EN_CURSO|FINALIZADO|CANCELADO")
    public List<CursoEventoResponse> listar(@RequestParam(required = false) String estado) {
        return cursoService.listar(estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un curso")
    public CursoEventoResponse obtener(@PathVariable Long id) {
        return cursoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear curso", description = "Queda PROGRAMADO. La fecha de inicio debe ser futura.")
    public CursoEventoResponse crear(@Valid @RequestBody CursoEventoRequest datos) {
        return cursoService.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar curso")
    public CursoEventoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CursoEventoRequest datos) {
        return cursoService.actualizar(id, datos);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del curso", description = "PROGRAMADO -> EN_CURSO -> FINALIZADO, o CANCELADO")
    public CursoEventoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest datos) {
        return cursoService.cambiarEstado(id, datos.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar curso", description = "409 si tiene inscripciones")
    public void eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
    }

    @GetMapping("/{id}/inscripciones")
    @Operation(summary = "Inscritos de un curso")
    public List<InscripcionResponse> inscripciones(@PathVariable Long id) {
        return inscripcionService.deCurso(id);
    }

    @PostMapping("/{id}/inscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscribir participante",
            description = "409 si no hay cupo, ya esta inscrito o el curso no esta PROGRAMADO")
    public InscripcionResponse inscribir(@PathVariable Long id, @Valid @RequestBody InscripcionRequest datos) {
        return inscripcionService.inscribir(id, datos.idUsuario());
    }
}
