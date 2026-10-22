package com.ecm.sigre.service;

import com.ecm.sigre.dto.CertificadoResponse;
import com.ecm.sigre.exception.RecursoNoEncontradoException;
import com.ecm.sigre.exception.ReglaNegocioException;
import com.ecm.sigre.model.Certificado;
import com.ecm.sigre.model.EstadoInscripcion;
import com.ecm.sigre.model.Inscripcion;
import com.ecm.sigre.repository.CertificadoRepository;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Emision y consulta de certificados. */
@Service
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final InscripcionService inscripcionService;
    private final UsuarioService usuarioService;

    public CertificadoService(CertificadoRepository certificadoRepository, InscripcionService inscripcionService,
                              UsuarioService usuarioService) {
        this.certificadoRepository = certificadoRepository;
        this.inscripcionService = inscripcionService;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<CertificadoResponse> listar() {
        return certificadoRepository.findAllByOrderByFechaEmisionDesc().stream().map(CertificadoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<CertificadoResponse> deUsuario(Long idUsuario) {
        usuarioService.buscar(idUsuario);
        return certificadoRepository.findByInscripcionUsuarioIdOrderByFechaEmisionDesc(idUsuario).stream()
                .map(CertificadoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CertificadoResponse obtener(Long id) {
        return CertificadoResponse.de(certificadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el certificado con id " + id)));
    }

    /** Verifica la autenticidad de un certificado por su codigo. */
    @Transactional(readOnly = true)
    public CertificadoResponse verificar(String codigo) {
        return CertificadoResponse.de(certificadoRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un certificado con el codigo " + codigo)));
    }

    /**
     * Emite el certificado de una inscripcion APROBADA. Solo se emite uno por inscripcion.
     */
    @Transactional
    public CertificadoResponse emitir(Long idInscripcion) {
        Inscripcion inscripcion = inscripcionService.buscar(idInscripcion);
        if (inscripcion.getEstado() != EstadoInscripcion.APROBADO) {
            throw new ReglaNegocioException("Solo se emiten certificados a inscripciones APROBADAS (estado actual: "
                    + inscripcion.getEstado() + ")");
        }
        if (certificadoRepository.existsByInscripcionId(idInscripcion)) {
            throw new ReglaNegocioException("La inscripcion ya tiene un certificado emitido");
        }
        return CertificadoResponse.de(certificadoRepository.save(new Certificado(inscripcion, nuevoCodigo())));
    }

    /** Codigo legible y unico, por ejemplo ECM-2026-4F7A9C2B. */
    private String nuevoCodigo() {
        return "ECM-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
