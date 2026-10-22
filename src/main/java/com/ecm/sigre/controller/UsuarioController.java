package com.ecm.sigre.controller;

import com.ecm.sigre.dto.CertificadoResponse;
import com.ecm.sigre.dto.InscripcionResponse;
import com.ecm.sigre.dto.UsuarioRequest;
import com.ecm.sigre.dto.UsuarioResponse;
import com.ecm.sigre.service.CertificadoService;
import com.ecm.sigre.service.InscripcionService;
import com.ecm.sigre.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CRUD de usuarios y consultas de sus inscripciones y certificados. */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Participantes, instructores y administradores")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final InscripcionService inscripcionService;
    private final CertificadoService certificadoService;

    public UsuarioController(UsuarioService usuarioService, InscripcionService inscripcionService,
                             CertificadoService certificadoService) {
        this.usuarioService = usuarioService;
        this.inscripcionService = inscripcionService;
        this.certificadoService = certificadoService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Filtro opcional ?rol=ASISTENTE|INSTRUCTOR|ADMIN")
    public List<UsuarioResponse> listar(@RequestParam(required = false) String rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un usuario")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Si la contrasena viene vacia se conserva la actual")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario", description = "409 si tiene inscripciones o es instructor")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    @GetMapping("/{id}/inscripciones")
    @Operation(summary = "Inscripciones de un usuario")
    public List<InscripcionResponse> inscripciones(@PathVariable Long id) {
        return inscripcionService.deUsuario(id);
    }

    @GetMapping("/{id}/certificados")
    @Operation(summary = "Certificados de un usuario")
    public List<CertificadoResponse> certificados(@PathVariable Long id) {
        return certificadoService.deUsuario(id);
    }
}
