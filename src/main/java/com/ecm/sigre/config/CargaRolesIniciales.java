package com.ecm.sigre.config;

import com.ecm.sigre.usuarios.model.Rol;
import com.ecm.sigre.usuarios.repository.RolRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registra los cuatro roles del sistema la primera vez que se inicia la aplicacion
 * (la tabla {@code rol} es un dato de referencia que el sistema necesita para funcionar).
 */
@Component
@Order(1)
public class CargaRolesIniciales implements ApplicationRunner {

    private static final List<String> ROLES = List.of("ADMIN", "COORDINADOR", "INSTRUCTOR", Rol.ASISTENTE);

    private final RolRepository rolRepositorio;

    /**
     * Crea el cargador con el repositorio de roles.
     *
     * @param rolRepositorio repositorio de roles
     */
    public CargaRolesIniciales(RolRepository rolRepositorio) {
        this.rolRepositorio = rolRepositorio;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String nombre : ROLES) {
            if (rolRepositorio.findByNombre(nombre).isEmpty()) {
                rolRepositorio.save(new Rol(nombre));
            }
        }
    }
}
