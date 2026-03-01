package com.login.gymcrm;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;

public class CsvSnapshotWriter {

    public void save(Path csvPath,
                     List<Trainee> trainees,
                     List<Trainer> trainers,
                     List<Training> trainings) throws IOException {
        Files.createDirectories(csvPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(
                csvPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            writer.write("# type,id,firstName,lastName,active|specialization|...,date,minutes");
            writer.newLine();

            trainees.stream()
                    .sorted(Comparator.comparing(Trainee::getId))
                    .forEach(t -> writeLine(writer, String.join(",",
                            "trainee",
                            safe(t.getId()),
                            safe(t.getFirstName()),
                            safe(t.getLastName()),
                            String.valueOf(t.isActive())
                    )));

            trainers.stream()
                    .sorted(Comparator.comparing(Trainer::getId))
                    .forEach(t -> writeLine(writer, String.join(",",
                            "trainer",
                            safe(t.getId()),
                            safe(t.getFirstName()),
                            safe(t.getLastName()),
                            safe(t.getSpecialization())
                    )));

            trainings.stream()
                    .sorted(Comparator.comparing(Training::getId))
                    .forEach(t -> writeLine(writer, String.join(",",
                            "training",
                            safe(t.getId()),
                            safe(t.getTraineeId()),
                            safe(t.getTrainerId()),
                            safe(t.getName()),
                            String.valueOf(t.getDate()),
                            String.valueOf(t.getDurationMinutes())
                    )));
        }
    }

    private static void writeLine(BufferedWriter writer, String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }
}
