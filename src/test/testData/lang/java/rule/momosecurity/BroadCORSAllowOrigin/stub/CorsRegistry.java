package org.springframework.web.servlet.config.annotation;

import org.springframework.web.servlet.config.annotation.CorsRegistration;

public class CorsRegistry {
    public CorsRegistration addMapping(String pathPattern) {
        return new CorsRegistration(pathPattern);
    }

}