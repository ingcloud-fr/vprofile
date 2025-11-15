package com.visualpathit.account.config;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring MVC pour servir les ressources statiques
 * notamment les photos de profil uploadées par les utilisateurs
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ServletContext servletContext;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les photos de profil uploadées depuis le volume Docker persistant
        // Ce répertoire est monté comme volume dans docker-compose.yml
        // IMPORTANT: file:/// avec 3 slashes pour les chemins absolus
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:///var/lib/facelink/uploads/")
            .setCachePeriod(3600);

        // Get the real path to webapp resources
        String resourcePath = servletContext.getRealPath("/resources/");

        if (resourcePath != null) {
            // Servir les ressources statiques depuis /webapp/resources/
            registry
                .addResourceHandler("/resources/**")
                .addResourceLocations("file:" + resourcePath + "/")
                .setCachePeriod(3600);
        }

        // Fallback to default locations
        registry
            .addResourceHandler("/resources/**")
            .addResourceLocations("/resources/");
    }
}
