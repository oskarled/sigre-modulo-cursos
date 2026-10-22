package com.ecm.sigre.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Datos para crear o actualizar un curso. Las fechas usan el formato AAAA-MM-DDTHH:mm:ss.
 */
public record CursoEventoRequest(
        @NotBlank(message = "El nombre del curso es obligatorio")
        @Size(max = 150, message = "El nombre del curso admite maximo 150 caracteres")
        String nombreCurso,

        @NotNull(message = "La duracion es obligatoria")
        @Min(value = 1, message = "La duracion debe ser de al menos 1 hora")
        @Max(value = 500, message = "La duracion no puede superar 500 horas")
        Integer duracionHoras,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fechaFin,

        @NotBlank(message = "El lugar es obligatorio")
        @Size(max = 150, message = "El lugar admite maximo 150 caracteres")
        String lugar,

        @NotNull(message = "El cupo maximo es obligatorio")
        @Min(value = 1, message = "El cupo maximo debe ser de al menos 1")
        @Max(value = 500, message = "El cupo maximo no puede superar 500")
        Integer cupoMaximo,

        @NotNull(message = "El instructor es obligatorio")
        Long idInstructor) {
}
