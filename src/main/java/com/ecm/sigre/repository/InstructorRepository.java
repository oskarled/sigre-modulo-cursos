package com.ecm.sigre.repository;

import com.ecm.sigre.model.Instructor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla instructor. */
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByUsuarioId(Long idUsuario);

    List<Instructor> findAllByOrderByUsuarioNombre();
}
