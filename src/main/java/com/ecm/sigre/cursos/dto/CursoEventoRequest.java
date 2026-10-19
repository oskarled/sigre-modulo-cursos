package com.ecm.sigre.cursos.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos que envia el administrador para crear o editar un curso (HU-05 y HU-06).
 *
 * @param nombreCurso   nombre del curso
 * @param duracionHoras intensidad horaria (mayor que cero)
 * @param fechaInicio   fecha y hora de inicio (debe ser posterior a la actual)
 * @param fechaFin      fecha y hora de finalizacion (posterior al inicio)
 * @param lugar         lugar donde se dicta
 * @param cupoMaximo    cupo maximo (entero positivo)
 * @param idInstructor  identificador del instructor a cargo
 */
public record CursoEventoRequest(
        @NotBlank(message = "El nombre del curso es obligatorio")
        @Size(max = 150, message = "El nombre del curso no puede superar 150 caracteres")
        String nombreCurso,

        @NotNull(message = "La duracion en horas es obligatoria")
        @Min(value = 1, message = "La duracion debe ser mayor que cero")
        Integer duracionHoras,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @Future(message = "La fecha de inicio debe ser posterior a la fecha actual")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fechaFin,

        @NotBlank(message = "El lugar es obligatorio")
        @Size(max = 150, message = "El lugar no puede superar 150 caracteres")
        String lugar,

        @NotNull(message = "El cupo maximo es obligatorio")
        @Min(value = 1, message = "El cupo maximo debe ser un entero mayor que cero")
        Integer cupoMaximo,

        @NotNull(message = "El instructor es obligatorio")
        Long idInstructor
) {
}
