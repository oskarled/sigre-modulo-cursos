package com.ecm.sigre.controller;

import com.ecm.sigre.dto.CursoEventoResponse;
import com.ecm.sigre.dto.InstructorRequest;
import com.ecm.sigre.dto.InstructorResponse;
import com.ecm.sigre.service.CursoEventoService;
import com.ecm.sigre.service.InstructorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CRUD de instructores. */
@RestController
@RequestMapping("/api/instructores")
@Tag(name = "Instructores", description = "Instructores que dictan los cursos")
public class InstructorController {

    private final InstructorService instructorService;
    private final CursoEventoService cursoService;

    public InstructorController(InstructorService instructorService, CursoEventoService cursoService) {
        this.instructorService = instructorService;
        this.cursoService = cursoService;
    }

    @GetMapping
    @Operation(summary = "Listar instructores")
    public List<InstructorResponse> listar() {
        return instructorService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un instructor")
    public InstructorResponse obtener(@PathVariable Long id) {
        return instructorService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear instructor", description = "Crea tambien su usuario con rol INSTRUCTOR")
    public InstructorResponse crear(@Valid @RequestBody InstructorRequest datos) {
        return instructorService.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar instructor")
    public InstructorResponse actualizar(@PathVariable Long id, @Valid @RequestBody InstructorRequest datos) {
        return instructorService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar instructor", description = "409 si tiene cursos asignados")
    public void eliminar(@PathVariable Long id) {
        instructorService.eliminar(id);
    }

    @GetMapping("/{id}/cursos")
    @Operation(summary = "Cursos de un instructor")
    public List<CursoEventoResponse> cursos(@PathVariable Long id) {
        return cursoService.listarPorInstructor(id);
    }
}
