package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.comment.CommentCreateRequest;
import com.incidentanalyzer.dto.comment.CommentResponse;
import com.incidentanalyzer.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents/{incidentId}/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "List comments", description = "Returns the collaborative comment thread for an incident.")
    public ResponseEntity<List<CommentResponse>> list(@PathVariable String incidentId) {
        return ResponseEntity.ok(commentService.listComments(incidentId));
    }

    @PostMapping
    @Operation(summary = "Add comment", description = "Adds a new comment to the incident thread.")
    public ResponseEntity<CommentResponse> create(@PathVariable String incidentId, @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.ok(commentService.createComment(incidentId, request));
    }
}
