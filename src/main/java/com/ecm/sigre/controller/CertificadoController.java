package com.ecm.sigre.controller;

import com.ecm.sigre.dto.CertificadoResponse;
import com.ecm.sigre.service.CertificadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Consulta y verificacion de certificados. */
@RestController
@RequestMapping("/api/certificados")
@Tag(name = "Certificados", description = "Certificados emitidos")
public class CertificadoController {

    private final CertificadoService certificadoService;

    public CertificadoController(CertificadoService certificadoService) {
        this.certificadoService = certificadoService;
    }

    @GetMapping
    @Operation(summary = "Listar certificados")
    public List<CertificadoResponse> listar() {
        return certificadoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un certificado")
    public CertificadoResponse obtener(@PathVariable Long id) {
        return certificadoService.obtener(id);
    }

    @GetMapping("/verificar/{codigo}")
    @Operation(summary = "Verificar certificado por codigo", description = "Ejemplo: ECM-2026-4F7A9C2B")
    public CertificadoResponse verificar(@PathVariable String codigo) {
        return certificadoService.verificar(codigo);
    }
}
