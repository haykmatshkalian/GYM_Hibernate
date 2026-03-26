package com.login.gymcrm.controller;

import com.login.gymcrm.dto.CredentialsResponse;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.dto.TrainerProfileResponse;
import com.login.gymcrm.dto.TrainerRegistrationRequest;
import com.login.gymcrm.dto.TrainerTrainingItemResponse;
import com.login.gymcrm.dto.UpdateTrainerProfileRequest;
import com.login.gymcrm.mapper.RestDtoMapper;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.service.TrainerService;
import com.login.gymcrm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Validated
@Tag(name = "Trainers", description = "Trainer REST endpoints")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final RestDtoMapper mapper;

    public TrainerController(TrainerService trainerService,
                             TrainingService trainingService,
                             RestDtoMapper mapper) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.mapper = mapper;
    }

    @Operation(summary = "Trainer Registration")
    @PostMapping
    public ResponseEntity<CredentialsResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        Trainer trainer = trainerService.createProfile(request.firstName(), request.lastName(), request.specialization());
        return ResponseEntity.ok(mapper.toCredentialsResponse(trainer.getUser()));
    }

    @Operation(summary = "Get Trainer Profile")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable("username") @NotBlank(message = "Username is required") String username) {
        Trainer trainer = trainerService.selectProfileByUsername(username);
        return ResponseEntity.ok(mapper.toTrainerProfileResponse(trainer));
    }

    @Operation(summary = "Update Trainer Profile (specialization is read-only)")
    @PutMapping
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(@Valid @RequestBody UpdateTrainerProfileRequest request) {
        Trainer trainer = trainerService.selectProfileByUsername(request.username());
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setActive(request.isActive());

        Trainer updated = trainerService.updateProfile(trainer);
        return ResponseEntity.ok(mapper.toTrainerProfileResponse(updated));
    }

    @Operation(summary = "Get Trainer Trainings List")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingItemResponse>> getTrainerTrainings(
            @PathVariable("username") @NotBlank(message = "Username is required") String username,
            @RequestParam(value = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(value = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(value = "traineeName", required = false) String traineeName) {

        List<TrainerTrainingItemResponse> response = trainingService.getTrainerTrainingsByCriteria(
                        username,
                        periodFrom,
                        periodTo,
                        traineeName)
                .stream()
                .map(mapper::toTrainerTrainingItemResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate/Deactivate Trainer By UserId (Toggle)")
    @PatchMapping("/user/{userId}/activation")
    public ResponseEntity<MessageResponse> toggleTrainerActivation(
            @PathVariable("userId") @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "UserId must be a valid UUID") String userId) {
        Trainer updated = trainerService.changeStateByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Trainer active state is now: " + updated.isActive()));
    }
}
