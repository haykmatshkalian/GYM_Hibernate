package com.login.gymcrm;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.facade.GymCrmFacade;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.security.exception.AuthorizationException;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleSmokeCheck {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
             Scanner scanner = new Scanner(System.in)) {

            GymCrmFacade facade = context.getBean(GymCrmFacade.class);
            Role currentRole = Role.ADMIN;
            facade.setCurrentRole(currentRole);

            if (isDemoMode(args)) {
                runPresentationScenario(facade, currentRole);
                facade.clearCurrentRole();
                return;
            }

            boolean running = true;
            while (running) {
                printMainMenu(currentRole);
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> currentRole = chooseRole(scanner, facade, currentRole);
                    case "2" -> traineeMenu(scanner, facade);
                    case "3" -> trainerMenu(scanner, facade);
                    case "4" -> trainingMenu(scanner, facade);
                    case "5" -> updateTraineeTrainersMenu(scanner, facade);
                    case "6" -> runPresentationScenario(facade, currentRole);
                    case "7" -> printAllCounts(facade);
                    case "8" -> printTrainingAndTrainingTypeReason();
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option");
                }
            }

            facade.clearCurrentRole();
            System.out.println("Bye.");
        }
    }

    private static void printMainMenu(Role currentRole) {
        System.out.println("\n=== GYM CRM INTERACTIVE DEMO ===");
        System.out.println("Current role: " + currentRole);
        System.out.println("1. Switch role (authorization demo)");
        System.out.println("2. Trainee actions");
        System.out.println("3. Trainer actions");
        System.out.println("4. Training actions");
        System.out.println("5. Update trainee trainers list");
        System.out.println("6. Run end-to-end presentation scenario");
        System.out.println("7. Print entity counts");
        System.out.println("8. Print reason for Training + TrainingType separation");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private static boolean isDemoMode(String[] args) {
        return args != null && args.length > 0 && "demo".equalsIgnoreCase(args[0]);
    }

    private static Role chooseRole(Scanner scanner, GymCrmFacade facade, Role currentRole) {
        System.out.println("\n--- Choose Role ---");
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            System.out.printf("%d. %s%n", i + 1, roles[i]);
        }
        System.out.print("Choose role number: ");

        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx < 1 || idx > roles.length) {
                System.out.println("Invalid role index.");
                return currentRole;
            }
            Role selected = roles[idx - 1];
            facade.setCurrentRole(selected);
            System.out.println("Role switched to: " + selected);
            return selected;
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number.");
            return currentRole;
        }
    }

    private static void traineeMenu(Scanner scanner, GymCrmFacade facade) {
        System.out.println("\n--- Trainee ---");
        System.out.println("1. Create");
        System.out.println("2. Update");
        System.out.println("3. Change state (toggle)");
        System.out.println("4. Delete");
        System.out.println("5. Select by id");
        System.out.println("6. List all");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("First name: ");
                    String first = scanner.nextLine();
                    System.out.print("Last name: ");
                    String last = scanner.nextLine();
                    Trainee created = facade.createTrainee(first, last);
                    printTrainee(created, "Created trainee");
                }
                case "2" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    Trainee existing = facade.selectTrainee(id);

                    System.out.print("New first name (current: " + existing.getFirstName() + "): ");
                    String first = scanner.nextLine();
                    System.out.print("New last name (current: " + existing.getLastName() + "): ");
                    String last = scanner.nextLine();

                    if (!first.isBlank()) {
                        existing.setFirstName(first.trim());
                    }
                    if (!last.isBlank()) {
                        existing.setLastName(last.trim());
                    }

                    Trainee updated = facade.updateTrainee(existing);
                    printTrainee(updated, "Updated trainee");
                }
                case "3" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    Trainee changed = facade.changeTraineeState(id);
                    printTrainee(changed, "Trainee state toggled");
                }
                case "4" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    facade.deleteTrainee(id);
                    System.out.println("Deleted trainee: " + id);
                }
                case "5" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    Trainee trainee = facade.selectTrainee(id);
                    printTrainee(trainee, "Found trainee");
                }
                case "6" -> facade.listTrainees().forEach(t -> printTrainee(t, "Trainee"));
                default -> System.out.println("Unknown option");
            }
        } catch (AuthorizationException ex) {
            System.out.println("Access denied: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    private static void trainerMenu(Scanner scanner, GymCrmFacade facade) {
        System.out.println("\n--- Trainer ---");
        System.out.println("1. Create");
        System.out.println("2. Update");
        System.out.println("3. Change state (toggle)");
        System.out.println("4. Select by id");
        System.out.println("5. List all");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("First name: ");
                    String first = scanner.nextLine();
                    System.out.print("Last name: ");
                    String last = scanner.nextLine();
                    System.out.print("Specialization: ");
                    String specialization = scanner.nextLine();
                    Trainer created = facade.createTrainer(first, last, specialization);
                    printTrainer(created, "Created trainer");
                }
                case "2" -> {
                    System.out.print("Trainer id: ");
                    String id = scanner.nextLine();
                    Trainer existing = facade.selectTrainer(id);

                    System.out.print("New first name (current: " + existing.getFirstName() + "): ");
                    String first = scanner.nextLine();
                    System.out.print("New last name (current: " + existing.getLastName() + "): ");
                    String last = scanner.nextLine();
                    System.out.print("New specialization (current: " + existing.getSpecialization() + "): ");
                    String specialization = scanner.nextLine();

                    if (!first.isBlank()) {
                        existing.setFirstName(first.trim());
                    }
                    if (!last.isBlank()) {
                        existing.setLastName(last.trim());
                    }
                    if (!specialization.isBlank()) {
                        existing.setSpecialization(specialization.trim());
                    }

                    Trainer updated = facade.updateTrainer(existing);
                    printTrainer(updated, "Updated trainer");
                }
                case "3" -> {
                    System.out.print("Trainer id: ");
                    String id = scanner.nextLine();
                    Trainer changed = facade.changeTrainerState(id);
                    printTrainer(changed, "Trainer state toggled");
                }
                case "4" -> {
                    System.out.print("Trainer id: ");
                    String id = scanner.nextLine();
                    Trainer trainer = facade.selectTrainer(id);
                    printTrainer(trainer, "Found trainer");
                }
                case "5" -> facade.listTrainers().forEach(t -> printTrainer(t, "Trainer"));
                default -> System.out.println("Unknown option");
            }
        } catch (AuthorizationException ex) {
            System.out.println("Access denied: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    private static void trainingMenu(Scanner scanner, GymCrmFacade facade) {
        System.out.println("\n--- Training ---");
        System.out.println("1. Create");
        System.out.println("2. Select by id");
        System.out.println("3. List all");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Trainee id: ");
                    String traineeId = scanner.nextLine();
                    System.out.print("Trainer id: ");
                    String trainerId = scanner.nextLine();
                    System.out.print("Training name: ");
                    String name = scanner.nextLine();
                    System.out.print("Date (yyyy-MM-dd): ");
                    String dateText = scanner.nextLine();
                    System.out.print("Duration minutes: ");
                    String durationText = scanner.nextLine();

                    LocalDate date = LocalDate.parse(dateText.trim());
                    int duration = Integer.parseInt(durationText.trim());

                    Training created = facade.createTraining(traineeId, trainerId, name, date, duration);
                    printTraining(created, "Created training");
                }
                case "2" -> {
                    System.out.print("Training id: ");
                    String id = scanner.nextLine();
                    Training training = facade.selectTraining(id);
                    printTraining(training, "Found training");
                }
                case "3" -> facade.listTrainings().forEach(t -> printTraining(t, "Training"));
                default -> System.out.println("Unknown option");
            }
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number for duration.");
        } catch (AuthorizationException ex) {
            System.out.println("Access denied: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    private static void updateTraineeTrainersMenu(Scanner scanner, GymCrmFacade facade) {
        try {
            System.out.print("Trainee id: ");
            String traineeId = scanner.nextLine().trim();
            System.out.print("Trainer ids (comma separated): ");
            String idsLine = scanner.nextLine().trim();

            List<String> trainerIds = Arrays.stream(idsLine.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            Trainee updated = facade.updateTraineeTrainers(traineeId, trainerIds);
            System.out.println("Updated trainers for trainee " + updated.getId() + ": " +
                    updated.getTrainers().stream().map(Trainer::getId).collect(Collectors.joining(", ")));
        } catch (AuthorizationException ex) {
            System.out.println("Access denied: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    private static void runPresentationScenario(GymCrmFacade facade, Role currentRole) {
        Role previous = currentRole;
        try {
            String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss", Locale.ROOT));

            System.out.println("\n=== PRESENTATION SCENARIO START ===");
            System.out.println("Run id: " + suffix);

            System.out.println("Step 1: Authorization with annotation-based role checks.");
            facade.setCurrentRole(Role.VIEWER);
            try {
                facade.createTrainee("Blocked", "User");
                System.out.println("Unexpected: VIEWER created trainee");
            } catch (AuthorizationException ex) {
                System.out.println("Authorization works: VIEWER cannot create trainee");
            }

            System.out.println("Step 2: Create trainee/trainer profiles as ADMIN.");
            facade.setCurrentRole(Role.ADMIN);
            Trainee trainee = facade.createTrainee("Demo" + suffix, "Trainee");
            Trainer t1 = facade.createTrainer("Demo" + suffix, "TrainerOne", "Cardio");
            Trainer t2 = facade.createTrainer("Demo" + suffix, "TrainerTwo", "Strength");
            printTrainee(trainee, "Created");
            printTrainer(t1, "Created");
            printTrainer(t2, "Created");

            System.out.println("Step 3: Update trainee-trainer join table.");
            facade.updateTraineeTrainers(trainee.getId(), List.of(t1.getId()));
            Trainee withOneTrainer = facade.selectTrainee(trainee.getId());
            System.out.println("Join rows after 1st update (trainer_id -> trainee_id): " +
                    joinRowsForTrainee(withOneTrainer));

            facade.updateTraineeTrainers(trainee.getId(), List.of(t1.getId(), t2.getId()));
            Trainee withTwoTrainers = facade.selectTrainee(trainee.getId());
            System.out.println("Join rows after 2nd update (trainer_id -> trainee_id): " +
                    joinRowsForTrainee(withTwoTrainers));

            System.out.println("Step 4: changeState() toggles every call (not idempotent).");
            Trainee afterToggle1 = facade.changeTraineeState(trainee.getId());
            Trainee afterToggle2 = facade.changeTraineeState(trainee.getId());
            System.out.println("Trainee state toggled twice: " + afterToggle1.isActive() + " -> " + afterToggle2.isActive());
            Trainer trainerAfterToggle1 = facade.changeTrainerState(t1.getId());
            Trainer trainerAfterToggle2 = facade.changeTrainerState(t1.getId());
            System.out.println("Trainer state toggled twice: " + trainerAfterToggle1.isActive() + " -> " + trainerAfterToggle2.isActive());

            System.out.println("Step 5: Create training and show data types + TrainingType relation.");
            Training training = facade.createTraining(trainee.getId(), t2.getId(), "Demo Session " + suffix, LocalDate.now(), 30);
            printTraining(training, "Created training");

            System.out.println("Step 6: Cascade delete (delete trainee removes related trainings).");
            facade.deleteTrainee(trainee.getId());
            try {
                facade.selectTraining(training.getId());
                System.out.println("Cascade delete (trainee -> trainings): FAILED");
            } catch (EntityNotFoundException ex) {
                System.out.println("Cascade delete (trainee -> trainings): OK");
            }

            System.out.println("Step 7: Why Training and TrainingType are separate tables.");
            printTrainingAndTrainingTypeReason();

            System.out.println("=== PRESENTATION SCENARIO END ===");
            System.out.println("Tip: check logs to see transactionId values for request chains.");
        } catch (Exception ex) {
            System.out.println("Scenario failed: " + ex.getMessage());
        } finally {
            facade.setCurrentRole(previous);
        }
    }

    private static void printAllCounts(GymCrmFacade facade) {
        System.out.println("\n=== COUNTS ===");
        System.out.println("Trainees: " + facade.listTrainees().size());
        System.out.println("Trainers: " + facade.listTrainers().size());
        System.out.println("Trainings: " + facade.listTrainings().size());
    }

    private static void printTrainee(Trainee trainee, String label) {
        System.out.println(label + ": id=" + trainee.getId()
                + ", name=" + trainee.getFirstName() + " " + trainee.getLastName()
                + ", username=" + trainee.getUsername()
                + ", active=" + trainee.isActive());
    }

    private static void printTrainer(Trainer trainer, String label) {
        System.out.println(label + ": id=" + trainer.getId()
                + ", name=" + trainer.getFirstName() + " " + trainer.getLastName()
                + ", username=" + trainer.getUsername()
                + ", specialization=" + trainer.getSpecialization()
                + ", active=" + trainer.isActive());
    }

    private static void printTraining(Training training, String label) {
        System.out.println(label + ": id=" + training.getId()
                + ", traineeId=" + training.getTraineeId()
                + ", trainerId=" + training.getTrainerId()
                + ", name=" + training.getName()
                + ", date=" + training.getDate()
                + ", durationMinutes=" + training.getDurationMinutes()
                + ", type=" + (training.getTrainingType() == null ? "n/a" : training.getTrainingType().getName()));
    }

    private static String joinRowsForTrainee(Trainee trainee) {
        return trainee.getTrainers().stream()
                .map(trainer -> trainer.getId() + " -> " + trainee.getId())
                .collect(Collectors.joining(", "));
    }

    private static void printTrainingAndTrainingTypeReason() {
        System.out.println("Training table stores each session (date, duration, trainee, trainer).");
        System.out.println("TrainingType table stores reusable type names (Cardio, Strength, etc).");
        System.out.println("One TrainingType can be referenced by many Training rows, which avoids duplicated type text and keeps type changes centralized.");
    }
}
