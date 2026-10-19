package com.ecm.sigre.usuarios.repository;

import com.ecm.sigre.usuarios.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de roles.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre.
     *
     * @param nombre nombre del rol (por ejemplo, ASISTENTE)
     * @return el rol, o vacio si no existe
     */
    Optional<Rol> findByNombre(String nombre);
}
