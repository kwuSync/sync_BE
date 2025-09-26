package com.sync_BE.repository;

import com.sync_BE.domain.NewsComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsCommentRepository extends MongoRepository<NewsComment, Integer> {
}
