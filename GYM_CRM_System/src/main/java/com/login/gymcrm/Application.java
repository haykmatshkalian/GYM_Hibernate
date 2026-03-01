package com.login.gymcrm;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.facade.GymCrmFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);
            log.info("Gym CRM system initialized. Trainees={}, Trainers={}, Trainings={}",
                    facade.listTrainees().size(), facade.listTrainers().size(), facade.listTrainings().size());
        }
    }
}
