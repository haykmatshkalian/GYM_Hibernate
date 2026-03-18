package com.login.gymcrm.controller;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.dto.CredentialsResponse;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.dto.TraineeProfileResponse;
import com.login.gymcrm.dto.TraineeRegistrationRequest;
import com.login.gymcrm.dto.TraineeTrainingItemResponse;
import com.login.gymcrm.dto.TrainerSummaryResponse;
import com.login.gymcrm.dto.UpdateTraineeProfileRequest;
import com.login.gymcrm.dto.UpdateTraineeTrainersRequest;
import com.login.gymcrm.mapper.RestDtoMapper;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/trainees")
@Validated
@Tag(name = "Trainees", description = "Trainee REST endpoints")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final RestDtoMapper mapper;

    public TraineeController(TraineeService traineeService,
                             TrainingService trainingService,
                             RestDtoMapper mapper) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Trainee Registration",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Trainee registered"),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = com.login.gymcrm.dto.ApiErrorResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<CredentialsResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee trainee = traineeService.createProfile(
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.address()
        );
        return ResponseEntity.ok(mapper.toCredentialsResponse(trainee.getUser()));
    }

    @Operation(summary = "Get Trainee Profile")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@PathVariable("username") String username) {
        Trainee trainee = traineeService.selectProfileByUsername(username);
        return ResponseEntity.ok(mapper.toTraineeProfileResponse(trainee));
    }

    @Operation(summary = "Update Trainee Profile")
    @PutMapping
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(@Valid @RequestBody UpdateTraineeProfileRequest request) {
        Trainee trainee = traineeService.selectProfileByUsername(request.username());
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.isActive());

        Trainee updated = traineeService.updateProfile(trainee);
        return ResponseEntity.ok(mapper.toTraineeProfileResponse(updated));
    }

    @Operation(summary = "Delete Trainee Profile")
    @DeleteMapping("/{username}")
    public ResponseEntity<MessageResponse> deleteTraineeProfile(@PathVariable("username") String username) {
        traineeService.deleteProfileByUsername(username);
        return ResponseEntity.ok(new MessageResponse("Trainee profile deleted"));
    }

    @Operation(summary = "Get Not Assigned Active Trainers For Trainee")
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedActiveTrainers(@PathVariable("username") String username) {
        List<TrainerSummaryResponse> response = traineeService.listUnassignedTrainersByTraineeUsername(username).stream()
                .map(mapper::toTrainerSummaryResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Trainee's Trainer List")
    @PutMapping("/trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTraineeTrainersList(
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        Trainee trainee = traineeService.updateTrainersListByUsernames(
                request.traineeUsername(),
                request.trainerUsernames()
        );

        List<TrainerSummaryResponse> response = trainee.getTrainers().stream()
                .map(mapper::toTrainerSummaryResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Trainee Trainings List")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingItemResponse>> getTraineeTrainings(
            @PathVariable("username") String username,
            @RequestParam(value = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(value = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(value = "trainerName", required = false) String trainerName,
            @RequestParam(value = "trainingType", required = false) String trainingType) {

        List<TraineeTrainingItemResponse> response = trainingService.getTraineeTrainingsByCriteria(
                        username,
                        periodFrom,
                        periodTo,
                        trainerName,
                        trainingType)
                .stream()
                .map(mapper::toTraineeTrainingItemResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate/Deactivate Trainee By UserId (Toggle)")
    @PatchMapping("/user/{userId}/activation")
    public ResponseEntity<MessageResponse> toggleTraineeActivation(@PathVariable("userId") String userId) {
        Trainee updated = traineeService.changeStateByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Trainee active state is now: " + updated.isActive()));
    }
}
