package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.auth.UserCreateRequest;
import com.incidentanalyzer.dto.auth.UserResponse;
import com.incidentanalyzer.model.User;
import com.incidentanalyzer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user", description = "Returns the current signed-in user profile.")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.toResponse(userService.currentUser()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users", description = "Admin-only user listing endpoint.")
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userService.findAll().stream().map(userService::toResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user", description = "Admin-only user creation endpoint with explicit role control.")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(request.password())
                .role(request.role())
                .build();
        return ResponseEntity.ok(userService.toResponse(userService.createUser(user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user", description = "Deletes a user account. Admin only.")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
