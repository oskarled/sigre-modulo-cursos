package com.ecm.sigre.cursos.repository;

import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de inscripciones. Las consultas se derivan del nombre de cada metodo.
 */
@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /**
     * Cuenta las inscripciones de un curso cuyo estado sea distinto del indicado.
     * Con {@code CANCELADO} se obtienen las inscripciones que ocupan cupo.
     *
     * @param idCurso identificador del curso
     * @param estado  estado que se excluye del conteo
     * @return numero de inscripciones
     */
    long countByCursoEventoIdAndEstadoNot(Long idCurso, EstadoInscripcion estado);

    /**
     * Busca la inscripcion de un usuario en un curso.
     *
     * @param idUsuario identificador del usuario
     * @param idCurso   identificador del curso
     * @return la inscripcion, o vacio si no existe
     */
    Optional<Inscripcion> findByUsuarioIdAndCursoEventoId(Long idUsuario, Long idCurso);

    /**
     * Lista las inscripciones de un usuario, de la mas reciente a la mas antigua.
     *
     * @param idUsuario identificador del usuario
     * @return inscripciones del usuario
     */
    List<Inscripcion> findByUsuarioIdOrderByFechaInscripcionDesc(Long idUsuario);

    /**
     * Lista las inscripciones de un curso, de la mas antigua a la mas reciente.
     *
     * @param idCurso identificador del curso
     * @return inscripciones del curso
     */
    List<Inscripcion> findByCursoEventoIdOrderByFechaInscripcionAsc(Long idCurso);
}
