package com.ecm.sigre.dto;

import com.ecm.sigre.model.CursoEvento;
import com.ecm.sigre.model.EstadoCurso;
import java.time.LocalDateTime;

/** Curso con los cupos ocupados y disponibles calculados. Coincide con el modelo del frontend. */
public record CursoEventoResponse(
        Long id,
        String nombreCurso,
        Integer duracionHoras,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String lugar,
        Integer cupoMaximo,
        long inscritos,
        long aforoDisponible,
        EstadoCurso estado,
        Long idInstructor,
        String nombreInstructor) {

    /**
     * Convierte la entidad en la respuesta de la API.
     *
     * @param c         curso
     * @param inscritos inscripciones activas (no canceladas)
     */
    public static CursoEventoResponse de(CursoEvento c, long inscritos) {
        return new CursoEventoResponse(c.getId(), c.getNombreCurso(), c.getDuracionHoras(), c.getFechaInicio(),
                c.getFechaFin(), c.getLugar(), c.getCupoMaximo(), inscritos,
                Math.max(0, c.getCupoMaximo() - inscritos), c.getEstado(),
                c.getInstructor().getId(), c.getInstructor().getUsuario().getNombre());
    }
}
