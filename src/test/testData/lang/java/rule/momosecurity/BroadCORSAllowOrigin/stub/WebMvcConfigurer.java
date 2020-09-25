package org.springframework.web.servlet.config.annotation;

import org.springframework.web.servlet.config.annotation.CorsRegistry;

public interface WebMvcConfigurer {

    default void addCorsMappings(CorsRegistry registry) {
    }
}
