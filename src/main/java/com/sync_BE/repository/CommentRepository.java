package com.sync_BE.repository;

import com.sync_BE.domain.Comment;
import com.sync_BE.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, ObjectId> {

    List<Comment> findByClusterIdOrderByCreatedAtDesc(String clusterId);

    long countByClusterId(String clusterId);

    List<Comment> findByUserOrderByCreatedAtDesc(User user);

    List<Comment> findByClusterIdAndUser(String clusterId, User user);

}
