package com.login.gymcrm.config;

import com.login.gymcrm.dao.TrainingTypeDao;
import com.login.gymcrm.model.TrainingType;
import com.login.gymcrm.util.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
public class TrainingTypeInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeInitializer.class);
    private static final List<String> DEFAULT_TYPES = List.of(
            "Cardio",
            "Strength",
            "Yoga",
            "Pilates",
            "HIIT",
            "Crossfit",
            "General"
    );

    private final TrainingTypeDao trainingTypeDao;
    private final UuidGenerator uuidGenerator;
    private final TransactionTemplate transactionTemplate;

    public TrainingTypeInitializer(TrainingTypeDao trainingTypeDao,
                                   UuidGenerator uuidGenerator,
                                   PlatformTransactionManager transactionManager) {
        this.trainingTypeDao = trainingTypeDao;
        this.uuidGenerator = uuidGenerator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> initializeDefaults());
    }

    private void initializeDefaults() {
        int inserted = 0;
        for (String typeName : DEFAULT_TYPES) {
            if (trainingTypeDao.findByName(typeName).isPresent()) {
                continue;
            }

            TrainingType trainingType = new TrainingType(uuidGenerator.generate(), typeName);
            trainingTypeDao.save(trainingType);
            inserted++;
        }

        if (inserted > 0) {
            log.info("Initialized {} missing training type(s)", inserted);
        }
    }
}
