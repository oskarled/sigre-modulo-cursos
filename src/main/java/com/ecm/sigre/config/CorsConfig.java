package com.ecm.sigre.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend de SIGRE (React + Vite) consuma la API desde otro origen.
 *
 * <p>Los origenes permitidos se configuran con la propiedad {@code sigre.cors.origenes}
 * o la variable de entorno {@code SIGRE_CORS_ORIGENES}, separados por comas.
 * Por defecto se permite el servidor de desarrollo de Vite.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenes;

    /**
     * Crea la configuracion con los origenes permitidos.
     *
     * @param origenes lista de origenes separada por comas
     */
    public CorsConfig(@Value("${sigre.cors.origenes:http://localhost:5173,http://localhost:4173}") String origenes) {
        this.origenes = origenes.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location");
    }
}
