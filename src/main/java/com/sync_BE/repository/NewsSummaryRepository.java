package com.sync_BE.repository;

import com.sync_BE.domain.NewsArticle;
import com.sync_BE.domain.NewsSummary;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsSummaryRepository extends MongoRepository<NewsSummary, ObjectId> {
    Optional<NewsSummary> findByArticle(NewsArticle article);

}
