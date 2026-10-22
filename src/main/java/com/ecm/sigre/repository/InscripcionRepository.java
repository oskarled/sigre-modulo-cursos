package com.ecm.sigre.repository;

import com.ecm.sigre.model.EstadoInscripcion;
import com.ecm.sigre.model.Inscripcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla inscripcion. */
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByCursoIdOrderByFechaInscripcion(Long idCurso);

    List<Inscripcion> findByUsuarioIdOrderByFechaInscripcionDesc(Long idUsuario);

    /** Cupos ocupados: inscripciones del curso que no estan canceladas. */
    long countByCursoIdAndEstadoNot(Long idCurso, EstadoInscripcion estado);

    boolean existsByCursoIdAndUsuarioIdAndEstadoNot(Long idCurso, Long idUsuario, EstadoInscripcion estado);

    boolean existsByCursoId(Long idCurso);

    boolean existsByUsuarioId(Long idUsuario);
}
