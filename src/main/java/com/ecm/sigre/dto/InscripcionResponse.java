package com.ecm.sigre.dto;

import com.ecm.sigre.model.EstadoInscripcion;
import com.ecm.sigre.model.Inscripcion;
import java.time.LocalDateTime;

/** Inscripcion con los datos del usuario y del curso. Coincide con el modelo del frontend. */
public record InscripcionResponse(
        Long id,
        Long idUsuario,
        String nombreUsuario,
        String correoUsuario,
        Long idCurso,
        String nombreCurso,
        String lugarCurso,
        LocalDateTime fechaInicioCurso,
        LocalDateTime fechaInscripcion,
        EstadoInscripcion estado) {

    /** Convierte la entidad en la respuesta de la API. */
    public static InscripcionResponse de(Inscripcion i) {
        return new InscripcionResponse(i.getId(), i.getUsuario().getId(), i.getUsuario().getNombre(),
                i.getUsuario().getCorreo(), i.getCurso().getId(), i.getCurso().getNombreCurso(),
                i.getCurso().getLugar(), i.getCurso().getFechaInicio(), i.getFechaInscripcion(), i.getEstado());
    }
}
