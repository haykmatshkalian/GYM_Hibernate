package com.login.gymcrm;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.facade.GymCrmFacade;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.CsvSnapshotWriter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import java.io.IOException;
import java.nio.file.Path;



public class ConsoleSmokeCheck {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
             Scanner scanner = new Scanner(System.in)) {

            GymCrmFacade facade = context.getBean(GymCrmFacade.class);
            String seedPath = context.getEnvironment().getProperty("storage.seed.path", "./config/seed-data.csv");
            Path csvPath = Path.of(seedPath).toAbsolutePath();
            CsvSnapshotWriter snapshotWriter = new CsvSnapshotWriter();


            boolean running = true;
            while (running) {
                printMainMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> traineeMenu(scanner, facade);
                    case "2" -> trainerMenu(scanner, facade);
                    case "3" -> trainingMenu(scanner, facade);
                    case "4" -> printAllCounts(facade);
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option");
                }
            }

            persistSnapshot(facade, snapshotWriter, csvPath);
            System.out.println("Bye.");
        }
    }

    private static void persistSnapshot(GymCrmFacade facade, CsvSnapshotWriter writer, Path csvPath) {
        try {
            writer.save(csvPath, facade.listTrainees(), facade.listTrainers(), facade.listTrainings());
            System.out.println("Data saved to: " + csvPath);
        } catch (IOException ex) {
            System.out.println("Failed to save data: " + ex.getMessage());
        }
    }


    private static void printMainMenu() {
        System.out.println("\n=== GYM CRM INTERACTIVE DEMO ===");
        System.out.println("1. Trainee actions (create/update/delete/select/list)");
        System.out.println("2. Trainer actions (create/update/select/list)");
        System.out.println("3. Training actions (create/select/list)");
        System.out.println("4. Print entity counts");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private static void traineeMenu(Scanner scanner, GymCrmFacade facade) {
        System.out.println("\n--- Trainee ---");
        System.out.println("1. Create");
        System.out.println("2. Update");
        System.out.println("3. Delete");
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
                    Trainee created = facade.createTrainee(first, last);
                    System.out.println("Created trainee: id=" + created.getId() + ", username=" + created.getUsername());
                }
                case "2" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    Trainee existing = facade.selectTrainee(id);

                    System.out.print("New first name (current: " + existing.getFirstName() + "): ");
                    String first = scanner.nextLine();
                    System.out.print("New last name (current: " + existing.getLastName() + "): ");
                    String last = scanner.nextLine();
                    System.out.print("Active (true/false, current: " + existing.isActive() + "): ");
                    String activeInput = scanner.nextLine();

                    if (!first.isBlank()) {
                        existing.setFirstName(first);
                    }
                    if (!last.isBlank()) {
                        existing.setLastName(last);
                    }
                    if (!activeInput.isBlank()) {
                        existing.setActive(Boolean.parseBoolean(activeInput));
                    }

                    Trainee updated = facade.updateTrainee(existing);
                    System.out.println("Updated trainee: id=" + updated.getId());
                }
                case "3" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    facade.deleteTrainee(id);
                    System.out.println("Deleted trainee: " + id);
                }
                case "4" -> {
                    System.out.print("Trainee id: ");
                    String id = scanner.nextLine();
                    Trainee trainee = facade.selectTrainee(id);
                    System.out.println("Found trainee: id=" + trainee.getId() + ", username=" + trainee.getUsername());
                }
                case "5" -> facade.listTrainees().forEach(t ->
                        System.out.println("id=" + t.getId() + ", name=" + t.getFirstName() + " " + t.getLastName() + ", username=" + t.getUsername()));
                default -> System.out.println("Unknown option");
            }
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    private static void trainerMenu(Scanner scanner, GymCrmFacade facade) {
        System.out.println("\n--- Trainer ---");
        System.out.println("1. Create");
        System.out.println("2. Update");
        System.out.println("3. Select by id");
        System.out.println("4. List all");
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
                    System.out.println("Created trainer: id=" + created.getId() + ", username=" + created.getUsername());
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
                        existing.setFirstName(first);
                    }
                    if (!last.isBlank()) {
                        existing.setLastName(last);
                    }
                    if (!specialization.isBlank()) {
                        existing.setSpecialization(specialization);
                    }

                    Trainer updated = facade.updateTrainer(existing);
                    System.out.println("Updated trainer: id=" + updated.getId());
                }
                case "3" -> {
                    System.out.print("Trainer id: ");
                    String id = scanner.nextLine();
                    Trainer trainer = facade.selectTrainer(id);
                    System.out.println("Found trainer: id=" + trainer.getId() + ", username=" + trainer.getUsername());
                }
                case "4" -> facade.listTrainers().forEach(t ->
                        System.out.println("id=" + t.getId() + ", name=" + t.getFirstName() + " " + t.getLastName() + ", username=" + t.getUsername()));
                default -> System.out.println("Unknown option");
            }
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

                    LocalDate date = LocalDate.parse(dateText);
                    int duration = Integer.parseInt(durationText);

                    Training created = facade.createTraining(traineeId, trainerId, name, date, duration);
                    System.out.println("Created training: id=" + created.getId());
                }
                case "2" -> {
                    System.out.print("Training id: ");
                    String id = scanner.nextLine();
                    Training training = facade.selectTraining(id);
                    System.out.println("Found training: id=" + training.getId() + ", name=" + training.getName());
                }
                case "3" -> facade.listTrainings().forEach(t ->
                        System.out.println("id=" + t.getId() + ", traineeId=" + t.getTraineeId()
                                + ", trainerId=" + t.getTrainerId() + ", name=" + t.getName()));
                default -> System.out.println("Unknown option");
            }
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number for duration.");
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    private static void printAllCounts(GymCrmFacade facade) {
        System.out.println("\n=== COUNTS ===");
        System.out.println("Trainees: " + facade.listTrainees().size());
        System.out.println("Trainers: " + facade.listTrainers().size());
        System.out.println("Trainings: " + facade.listTrainings().size());
    }
}
