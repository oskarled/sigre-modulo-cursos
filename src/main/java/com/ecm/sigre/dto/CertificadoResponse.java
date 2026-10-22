package com.ecm.sigre.dto;

import com.ecm.sigre.model.Certificado;
import java.time.LocalDateTime;

/** Certificado emitido con los datos necesarios para imprimirlo o verificarlo. */
public record CertificadoResponse(
        Long id,
        String codigo,
        LocalDateTime fechaEmision,
        Long idInscripcion,
        Long idUsuario,
        String nombreParticipante,
        String documentoParticipante,
        Long idCurso,
        String nombreCurso,
        Integer duracionHoras,
        String nombreInstructor) {

    /** Convierte la entidad en la respuesta de la API. */
    public static CertificadoResponse de(Certificado c) {
        var insc = c.getInscripcion();
        var curso = insc.getCurso();
        return new CertificadoResponse(c.getId(), c.getCodigo(), c.getFechaEmision(), insc.getId(),
                insc.getUsuario().getId(), insc.getUsuario().getNombre(), insc.getUsuario().getDocumento(),
                curso.getId(), curso.getNombreCurso(), curso.getDuracionHoras(),
                curso.getInstructor().getUsuario().getNombre());
    }
}
