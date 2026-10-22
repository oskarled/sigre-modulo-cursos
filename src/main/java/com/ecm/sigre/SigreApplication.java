package com.ecm.sigre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de SIGRE (Sistema de Registro para Eventos)
 * de E.C.M. Training Center S.A.S.
 */
@SpringBootApplication
public class SigreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigreApplication.class, args);
    }
}
