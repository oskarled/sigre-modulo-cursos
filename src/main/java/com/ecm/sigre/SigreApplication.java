package com.ecm.sigre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del backend del SIGRE (Sistema de Gestion de Registro para Eventos)
 * de E.C.M. Training Center S.A.S.
 *
 * <p>{@code @SpringBootApplication} activa la configuracion automatica de Spring Boot
 * y el escaneo de componentes del paquete {@code com.ecm.sigre}.</p>
 */
@SpringBootApplication
public class SigreApplication {

    /**
     * Punto de entrada de la aplicacion.
     *
     * @param args argumentos de la linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(SigreApplication.class, args);
    }
}
