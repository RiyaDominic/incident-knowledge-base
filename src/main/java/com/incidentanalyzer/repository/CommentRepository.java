package com.incidentanalyzer.repository;

import com.incidentanalyzer.model.Comment;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByIncidentIdOrderByTimestampAsc(String incidentId);
}
