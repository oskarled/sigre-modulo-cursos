package com.ecm.sigre.usuarios.repository;

import com.ecm.sigre.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
