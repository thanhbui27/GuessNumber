package com.guessgame;

import static org.assertj.core.api.Assertions.assertThat;

import com.guessgame.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class DefaultProfileContextTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(GuessGameApplication.class)
            .withPropertyValues(
                    "spring.datasource.url=jdbc:h2:mem:default_profile_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                    "spring.datasource.username=sa",
                    "spring.datasource.password=",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                    "spring.flyway.enabled=false"
            );

    @Test
    void contextLoadsWithoutExplicitJwtSecret() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(JwtService.class));
    }
}
