package com.visualpathit.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring MVC pour servir les ressources statiques
 * notamment les photos de profil uploadées par les utilisateurs
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les ressources statiques depuis /webapp/resources/
        registry
            .addResourceHandler("/resources/**")
            .addResourceLocations("/resources/", "classpath:/static/", "classpath:/public/");
    }
}
