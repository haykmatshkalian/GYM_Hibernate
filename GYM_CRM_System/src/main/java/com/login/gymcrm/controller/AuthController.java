package com.login.gymcrm.controller;

import com.login.gymcrm.dto.AuthTokenResponse;
import com.login.gymcrm.dto.ChangePasswordRequest;
import com.login.gymcrm.dto.LoginRequest;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.service.AuthenticationService;
import com.login.gymcrm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "Authentication and login management")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a user using username/password and returns JWT token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = com.login.gymcrm.dto.ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "423", description = "User is temporarily blocked", content = @Content(schema = @Schema(implementation = com.login.gymcrm.dto.ApiErrorResponse.class)))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request.username(), request.password()));
    }

    @Operation(summary = "Login (query parameters)", description = "Backward-compatible login endpoint")
    @GetMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
            @RequestParam("username") @NotBlank(message = "Username is required") String username,
            @RequestParam("password") @NotBlank(message = "Password is required") String password) {
        return ResponseEntity.ok(authenticationService.login(username, password));
    }

    @Operation(
            summary = "Change Login Password",
            description = "Changes user login password by username and old/new password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password changed"),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = com.login.gymcrm.dto.ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid old password", content = @Content(schema = @Schema(implementation = com.login.gymcrm.dto.ApiErrorResponse.class)))
            }
    )
    @PutMapping("/login")
    public ResponseEntity<MessageResponse> changeLogin(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request.username(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
