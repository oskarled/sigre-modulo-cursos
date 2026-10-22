package com.ecm.sigre.repository;

import com.ecm.sigre.model.NombreRol;
import com.ecm.sigre.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla usuario. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findByRolNombreOrderByNombre(NombreRol rol);

    List<Usuario> findAllByOrderByNombre();
}
