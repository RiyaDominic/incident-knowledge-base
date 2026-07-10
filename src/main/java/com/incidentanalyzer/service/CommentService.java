package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.comment.CommentCreateRequest;
import com.incidentanalyzer.dto.comment.CommentResponse;
import com.incidentanalyzer.exception.ResourceNotFoundException;
import com.incidentanalyzer.model.Comment;
import com.incidentanalyzer.repository.CommentRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final IncidentService incidentService;

    public CommentResponse createComment(String incidentId, CommentCreateRequest request) {
        incidentService.getIncidentEntity(incidentId);
        var currentUser = userService.currentUser();
        Comment comment = Comment.builder()
                .incidentId(incidentId)
                .userId(currentUser.getId())
                .message(request.message())
                .timestamp(Instant.now())
                .build();
        Comment saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    public List<CommentResponse> listComments(String incidentId) {
        incidentService.getIncidentEntity(incidentId);
        return commentRepository.findByIncidentIdOrderByTimestampAsc(incidentId).stream().map(this::toResponse).toList();
    }

    private CommentResponse toResponse(Comment comment) {
        String userName;
        try {
            userName = userService.findById(comment.getUserId()).getName();
        } catch (ResourceNotFoundException ex) {
            userName = "Unknown user";
        }
        return new CommentResponse(comment.getId(), comment.getIncidentId(), comment.getUserId(), userName, comment.getMessage(), comment.getTimestamp());
    }
}
