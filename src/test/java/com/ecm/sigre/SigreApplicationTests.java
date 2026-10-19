package com.ecm.sigre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecm.sigre.usuarios.repository.InstructorRepository;
import com.ecm.sigre.usuarios.repository.RolRepository;
import com.ecm.sigre.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de humo: verifica que el contexto de Spring Boot arranca con el perfil "dev"
 * (H2 en memoria) y que se cargan los roles y los datos de demostracion.
 */
@SpringBootTest
@ActiveProfiles("dev")
class SigreApplicationTests {

    @Autowired
    private RolRepository rolRepositorio;

    @Autowired
    private UsuarioRepository usuarioRepositorio;

    @Autowired
    private InstructorRepository instructorRepositorio;

    @Test
    void contextoArrancaYCargaDatosIniciales() {
        assertEquals(4, rolRepositorio.count());
        assertEquals(4, usuarioRepositorio.count());
        assertEquals(1, instructorRepositorio.count());
    }
}
