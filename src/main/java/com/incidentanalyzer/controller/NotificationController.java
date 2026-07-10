package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.notification.NotificationResponse;
import com.incidentanalyzer.service.NotificationService;
import com.incidentanalyzer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "List notifications", description = "Returns notifications for the current authenticated user.")
    public ResponseEntity<List<NotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userService.currentUser().getId()));
    }
}
