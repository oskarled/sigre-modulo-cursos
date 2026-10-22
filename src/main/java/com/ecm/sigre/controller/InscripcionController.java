package com.ecm.sigre.controller;

import com.ecm.sigre.dto.CambioEstadoRequest;
import com.ecm.sigre.dto.CertificadoResponse;
import com.ecm.sigre.dto.InscripcionResponse;
import com.ecm.sigre.service.CertificadoService;
import com.ecm.sigre.service.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Operaciones sobre una inscripcion: consultar, cancelar, cambiar estado y emitir certificado. */
@RestController
@RequestMapping("/api/inscripciones")
@Tag(name = "Inscripciones", description = "Inscripciones de participantes")
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final CertificadoService certificadoService;

    public InscripcionController(InscripcionService inscripcionService, CertificadoService certificadoService) {
        this.inscripcionService = inscripcionService;
        this.certificadoService = certificadoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una inscripcion")
    public InscripcionResponse obtener(@PathVariable Long id) {
        return inscripcionService.obtener(id);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar inscripcion", description = "Solo hasta 24 horas antes del inicio del curso")
    public InscripcionResponse cancelar(@PathVariable Long id) {
        return inscripcionService.cancelar(id);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de la inscripcion", description = "PENDIENTE -> PAGADO -> APROBADO | NO_APROBADO")
    public InscripcionResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest datos) {
        return inscripcionService.cambiarEstado(id, datos.estado());
    }

    @PostMapping("/{id}/certificado")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Emitir certificado", description = "Solo para inscripciones APROBADAS")
    public CertificadoResponse emitirCertificado(@PathVariable Long id) {
        return certificadoService.emitir(id);
    }
}
