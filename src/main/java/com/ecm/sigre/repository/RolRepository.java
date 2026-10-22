package com.ecm.sigre.repository;

import com.ecm.sigre.model.NombreRol;
import com.ecm.sigre.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla rol. */
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);
}
