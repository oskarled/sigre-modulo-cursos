package com.ecm.sigre.cursos.dto;

import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.model.Inscripcion;

import java.time.LocalDateTime;

/**
 * Datos de una inscripcion que se devuelven al cliente.
 *
 * @param id                identificador de la inscripcion
 * @param idUsuario         identificador del participante
 * @param nombreUsuario     nombre completo del participante
 * @param correoUsuario     correo del participante
 * @param idCurso           identificador del curso
 * @param nombreCurso       nombre del curso
 * @param lugarCurso        lugar del curso
 * @param fechaInicioCurso  fecha y hora de inicio del curso
 * @param fechaInscripcion  fecha y hora de la inscripcion
 * @param estado            estado de la inscripcion
 */
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
        EstadoInscripcion estado
) {

    /**
     * Convierte una inscripcion en su representacion de respuesta.
     *
     * @param inscripcion entidad a convertir
     * @return respuesta lista para enviarse al cliente
     */
    public static InscripcionResponse desde(Inscripcion inscripcion) {
        return new InscripcionResponse(
                inscripcion.getId(),
                inscripcion.getUsuario().getId(),
                inscripcion.getUsuario().nombreCompleto(),
                inscripcion.getUsuario().getCorreo(),
                inscripcion.getCursoEvento().getId(),
                inscripcion.getCursoEvento().getNombre(),
                inscripcion.getCursoEvento().getLugar(),
                inscripcion.getCursoEvento().getFechaInicio(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getEstado()
        );
    }
}
