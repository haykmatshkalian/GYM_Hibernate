package com.login.gymcrm.controller;

import com.login.gymcrm.dto.AddTrainingRequest;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.dto.TrainingTypeResponse;
import com.login.gymcrm.mapper.RestDtoMapper;
import com.login.gymcrm.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@Validated
@Tag(name = "Trainings", description = "Training REST endpoints")
public class TrainingController {

    private final TrainingService trainingService;
    private final RestDtoMapper mapper;

    public TrainingController(TrainingService trainingService, RestDtoMapper mapper) {
        this.trainingService = trainingService;
        this.mapper = mapper;
    }

    @Operation(summary = "Add Training")
    @PostMapping
    public ResponseEntity<MessageResponse> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        trainingService.createTrainingByUsernames(
                request.traineeUsername(),
                request.trainerUsername(),
                request.trainingName(),
                request.trainingDate(),
                request.trainingDuration()
        );

        return ResponseEntity.ok(new MessageResponse("Training added"));
    }

    @Operation(summary = "Get Training Types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingTypeResponse> response = trainingService.listTrainingTypes().stream()
                .map(mapper::toTrainingTypeResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
