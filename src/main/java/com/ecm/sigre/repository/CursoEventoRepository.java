package com.ecm.sigre.repository;

import com.ecm.sigre.model.CursoEvento;
import com.ecm.sigre.model.EstadoCurso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla curso_evento. */
public interface CursoEventoRepository extends JpaRepository<CursoEvento, Long> {

    List<CursoEvento> findAllByOrderByFechaInicio();

    List<CursoEvento> findByEstadoOrderByFechaInicio(EstadoCurso estado);

    List<CursoEvento> findByInstructorIdOrderByFechaInicio(Long idInstructor);

    boolean existsByInstructorId(Long idInstructor);
}
