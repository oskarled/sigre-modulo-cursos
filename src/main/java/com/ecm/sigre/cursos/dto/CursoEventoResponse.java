package com.ecm.sigre.cursos.dto;

import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoCurso;

import java.time.LocalDateTime;

/**
 * Datos de un curso que se devuelven al cliente, incluido el aforo disponible.
 *
 * @param id               identificador del curso
 * @param nombreCurso      nombre del curso
 * @param duracionHoras    intensidad horaria
 * @param fechaInicio      fecha y hora de inicio
 * @param fechaFin         fecha y hora de finalizacion
 * @param lugar            lugar donde se dicta
 * @param cupoMaximo       cupo maximo
 * @param inscritos        inscripciones que ocupan cupo
 * @param aforoDisponible  cupos libres
 * @param estado           estado del curso
 * @param idInstructor     identificador del instructor a cargo
 * @param nombreInstructor nombre completo del instructor
 */
public record CursoEventoResponse(
        Long id,
        String nombreCurso,
        Integer duracionHoras,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String lugar,
        Integer cupoMaximo,
        long inscritos,
        int aforoDisponible,
        EstadoCurso estado,
        Long idInstructor,
        String nombreInstructor
) {

    /**
     * Convierte un curso en su representacion de respuesta.
     *
     * @param curso     entidad a convertir
     * @param inscritos numero de inscripciones que ocupan cupo
     * @return respuesta lista para enviarse al cliente
     */
    public static CursoEventoResponse desde(CursoEvento curso, long inscritos) {
        return new CursoEventoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getDuracionHoras(),
                curso.getFechaInicio(),
                curso.getFechaFin(),
                curso.getLugar(),
                curso.getCupoMaximo(),
                inscritos,
                curso.obtenerAforoDisponible(inscritos),
                curso.getEstado(),
                curso.getInstructor().getId(),
                curso.getInstructor().getUsuario().nombreCompleto()
        );
    }
}
