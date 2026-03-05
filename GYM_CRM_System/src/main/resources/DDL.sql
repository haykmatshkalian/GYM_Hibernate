CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DROP TABLE IF EXISTS trainee_trainer, trainings, training_types, trainers, trainees, users;

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       username VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       is_active BOOLEAN NOT NULL
);


CREATE TABLE trainees (
                          id UUID PRIMARY KEY,
                          date_of_birth DATE,
                          address VARCHAR(255),
                          user_id UUID NOT NULL UNIQUE,

                          CONSTRAINT fk_trainee_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);


CREATE TABLE trainers (
                          id UUID PRIMARY KEY,
                          specialization VARCHAR(150),
                          user_id UUID NOT NULL UNIQUE,

                          CONSTRAINT fk_trainer_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);


CREATE TABLE training_types (
                                id UUID PRIMARY KEY,
                                name VARCHAR(150) NOT NULL UNIQUE
);


CREATE TABLE trainings (
                           id UUID PRIMARY KEY,
                           trainee_id UUID NOT NULL,
                           trainer_id UUID NOT NULL,
                           training_type_id UUID NOT NULL,
                           training_name VARCHAR(200) NOT NULL,
                           training_date DATE NOT NULL,
                           duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),

                           CONSTRAINT fk_training_trainee
                               FOREIGN KEY (trainee_id)
                                   REFERENCES trainees(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_training_trainer
                               FOREIGN KEY (trainer_id)
                                   REFERENCES trainers(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_training_type
                               FOREIGN KEY (training_type_id)
                                   REFERENCES training_types(id)
);

CREATE TABLE trainee_trainer (
                                 trainee_id UUID NOT NULL,
                                 trainer_id UUID NOT NULL,

                                 PRIMARY KEY (trainee_id, trainer_id),

                                 CONSTRAINT fk_tt_trainee
                                     FOREIGN KEY (trainee_id)
                                         REFERENCES trainees(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_tt_trainer
                                     FOREIGN KEY (trainer_id)
                                         REFERENCES trainers(id)
                                         ON DELETE CASCADE
);



