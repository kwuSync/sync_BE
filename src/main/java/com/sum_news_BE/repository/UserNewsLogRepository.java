package com.sum_news_BE.repository;

import com.sum_news_BE.domain.NewsArticle;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.domain.UserNewsLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNewsLogRepository extends MongoRepository<UserNewsLog, ObjectId> {

    List<UserNewsLog> findByUserId(String userId);

    Optional<UserNewsLog> findByUserAndArticle(User user, NewsArticle newsArticle);
}
