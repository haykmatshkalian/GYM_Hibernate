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

import static org.assertj.core.api.Assertions.assertThat;

class CsvSnapshotWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void saveCreatesDirectoriesAndWritesSortedCsvSnapshot() throws Exception {
        CsvSnapshotWriter writer = new CsvSnapshotWriter();

        Trainee traineeB = new Trainee("t2", "Alex,One", "Stone", "u1", "p", true);
        Trainee traineeA = new Trainee("t1", "Mia", "Ray", "u2", "p", false);

        Trainer trainerB = new Trainer("r2", "Sarah", "Cole", "u3", "p", "Yoga");
        Trainer trainerA = new Trainer("r1", "Leo", "King", "u4", "p", "Strength,Core");

        Training trainingB = new Training("tr2", "t2", "r2", "Morning,Session", LocalDate.of(2026, 1, 3), 30);
        Training trainingA = new Training("tr1", "t1", "r1", "Evening", LocalDate.of(2026, 1, 2), 45);

        Path csvPath = tempDir.resolve("snapshots/state.csv");
        writer.save(csvPath,
                List.of(traineeB, traineeA),
                List.of(trainerB, trainerA),
                List.of(trainingB, trainingA));

        assertThat(Files.exists(csvPath)).isTrue();

        List<String> lines = Files.readAllLines(csvPath);
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).startsWith("# type,id");

        assertThat(lines).containsSubsequence(
                "trainee,t1,Mia,Ray,false",
                "trainee,t2,Alex One,Stone,true"
        );
        assertThat(lines).containsSubsequence(
                "trainer,r1,Leo,King,Strength Core",
                "trainer,r2,Sarah,Cole,Yoga"
        );
        assertThat(lines).containsSubsequence(
                "training,tr1,t1,r1,Evening,2026-01-02,45",
                "training,tr2,t2,r2,Morning Session,2026-01-03,30"
        );
    }
}
