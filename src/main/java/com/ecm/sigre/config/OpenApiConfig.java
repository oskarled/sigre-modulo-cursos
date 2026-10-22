package com.ecm.sigre.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos generales de la documentacion Swagger (http://localhost:8080/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sigreOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API SIGRE - Sistema de Registro para Eventos")
                .version("1.0.0")
                .description("Servicios web de E.C.M. Training Center: autenticacion, usuarios, instructores, "
                        + "cursos, inscripciones y certificados. Proyecto formativo SENA, ficha 3134533.")
                .contact(new Contact().name("Oscar Ledesma Puello").email("oscarled@gmail.com")));
    }
}
