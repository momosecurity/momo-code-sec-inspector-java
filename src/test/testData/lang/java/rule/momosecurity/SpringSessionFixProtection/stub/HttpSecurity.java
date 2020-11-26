package org.springframework.security.config.annotation.web.builders;

import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;

public class HttpSecurity {
    public SessionManagementConfigurer<HttpSecurity> sessionManagement() throws Exception {
        return new SessionManagementConfigurer();
    }
}