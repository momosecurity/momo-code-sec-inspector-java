package org.springframework.security.config.annotation.web.configurers;

public class SessionManagementConfigurer<H> {

    public SessionManagementConfigurer() {

    }

    public SessionManagementConfigurer<H>.SessionFixationConfigurer sessionFixation() {
        return new SessionManagementConfigurer.SessionFixationConfigurer();
    }

    public final class SessionFixationConfigurer {
        public SessionFixationConfigurer() {
        }

        public SessionManagementConfigurer<H> newSession() {
            return SessionManagementConfigurer.this;
        }

        public SessionManagementConfigurer<H> migrateSession() {
            return SessionManagementConfigurer.this;
        }

        public SessionManagementConfigurer<H> changeSessionId() {
            return SessionManagementConfigurer.this;
        }

        public SessionManagementConfigurer<H> none() {
            return SessionManagementConfigurer.this;
        }
    }
}