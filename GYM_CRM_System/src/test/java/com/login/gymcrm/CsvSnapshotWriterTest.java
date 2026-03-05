package com.login.gymcrm;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CsvSnapshotWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void saveCreatesDirectoriesAndWritesSortedCsvSnapshot() throws Exception {
        CsvSnapshotWriter writer = new CsvSnapshotWriter();

        String traineeIdA = UUID.randomUUID().toString();
        String traineeIdB = UUID.randomUUID().toString();
        String trainerIdA = UUID.randomUUID().toString();
        String trainerIdB = UUID.randomUUID().toString();
        String trainingIdA = UUID.randomUUID().toString();
        String trainingIdB = UUID.randomUUID().toString();

        Trainee traineeB = new Trainee(traineeIdB, "Alex,One", "Stone", "u1", "p", true);
        Trainee traineeA = new Trainee(traineeIdA, "Mia", "Ray", "u2", "p", false);

        Trainer trainerB = new Trainer(trainerIdB, "Sarah", "Cole", "u3", "p", "Yoga");
        Trainer trainerA = new Trainer(trainerIdA, "Leo", "King", "u4", "p", "Strength,Core");

        Training trainingB = new Training(trainingIdB, traineeIdB, trainerIdB, "Morning,Session", LocalDate.of(2026, 1, 3), 30);
        Training trainingA = new Training(trainingIdA, traineeIdA, trainerIdA, "Evening", LocalDate.of(2026, 1, 2), 45);

        Path csvPath = tempDir.resolve("snapshots/state.csv");
        writer.save(csvPath,
                List.of(traineeB, traineeA),
                List.of(trainerB, trainerA),
                List.of(trainingB, trainingA));

        assertThat(Files.exists(csvPath)).isTrue();

        List<String> lines = Files.readAllLines(csvPath);
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).startsWith("# type,id");

        String traineeLineA = "trainee," + traineeIdA + ",Mia,Ray,false";
        String traineeLineB = "trainee," + traineeIdB + ",Alex One,Stone,true";
        String trainerLineA = "trainer," + trainerIdA + ",Leo,King,Strength Core";
        String trainerLineB = "trainer," + trainerIdB + ",Sarah,Cole,Yoga";
        String trainingLineA = "training," + trainingIdA + "," + traineeIdA + "," + trainerIdA + ",Evening,2026-01-02,45";
        String trainingLineB = "training," + trainingIdB + "," + traineeIdB + "," + trainerIdB + ",Morning Session,2026-01-03,30";

        assertThat(lines).contains(traineeLineA, traineeLineB);
        assertThat(lines).contains(trainerLineA, trainerLineB);
        assertThat(lines).contains(trainingLineA, trainingLineB);
    }
}
