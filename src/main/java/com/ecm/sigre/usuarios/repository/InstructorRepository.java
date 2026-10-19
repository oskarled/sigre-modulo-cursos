package com.ecm.sigre.usuarios.repository;

import com.ecm.sigre.usuarios.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de instructores.
 */
@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
