package com.login.gymcrm.facade;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.security.exception.AuthorizationException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacadeTest {

    @Test
    void annotationBasedAuthorizationBlocksForbiddenAction() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            facade.setCurrentRole(Role.VIEWER);
            assertThatThrownBy(() -> facade.createTrainee("Mia", "Lee"))
                    .isInstanceOf(AuthorizationException.class);
            facade.clearCurrentRole();
        }
    }
}
