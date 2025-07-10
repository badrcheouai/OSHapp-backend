
package com.ohse.OSHapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    private static final String ALLOWED_ORIGIN = "http://localhost:3000";
    private static final String ALL_PATHS = "/**";
    private static final String ALL_METHODS_AND_HEADERS = "*";

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping(ALL_PATHS)
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders(ALL_METHODS_AND_HEADERS);
                        // .allowCredentials(true); // Not allowed with allowedOrigins("*")
            }
        };
    }
}