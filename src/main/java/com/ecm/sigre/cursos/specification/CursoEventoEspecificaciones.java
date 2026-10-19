package com.ecm.sigre.cursos.specification;

import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoCurso;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Construye la busqueda dinamica de cursos (HU-08): cada filtro solo se aplica
 * cuando se envia un valor.
 */
public final class CursoEventoEspecificaciones {

    private CursoEventoEspecificaciones() {
        // Clase de utilidad: no se instancia
    }

    /**
     * Crea la especificacion con los filtros indicados.
     *
     * @param nombre          texto contenido en el nombre del curso (opcional)
     * @param lugar           texto contenido en el lugar (opcional)
     * @param desdeInclusive  fecha de inicio minima, incluida (opcional)
     * @param hastaExclusivo  fecha de inicio maxima, excluida (opcional)
     * @param estado          estado del curso (opcional)
     * @return especificacion lista para usar en el repositorio
     */
    public static Specification<CursoEvento> conFiltros(String nombre, String lugar,
                                                        LocalDateTime desdeInclusive,
                                                        LocalDateTime hastaExclusivo,
                                                        EstadoCurso estado) {
        return (raiz, consulta, constructor) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (nombre != null && !nombre.isBlank()) {
                condiciones.add(constructor.like(
                        constructor.lower(raiz.<String>get("nombre")),
                        "%" + nombre.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (lugar != null && !lugar.isBlank()) {
                condiciones.add(constructor.like(
                        constructor.lower(raiz.<String>get("lugar")),
                        "%" + lugar.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (desdeInclusive != null) {
                condiciones.add(constructor.greaterThanOrEqualTo(
                        raiz.<LocalDateTime>get("fechaInicio"), desdeInclusive));
            }
            if (hastaExclusivo != null) {
                condiciones.add(constructor.lessThan(
                        raiz.<LocalDateTime>get("fechaInicio"), hastaExclusivo));
            }
            if (estado != null) {
                condiciones.add(constructor.equal(raiz.<EstadoCurso>get("estado"), estado));
            }

            return constructor.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
