package com.sum_news_BE.repository;

import com.sum_news_BE.domain.NewsSummary;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsSummaryRepository extends MongoRepository<NewsSummary, ObjectId> {
    Optional<NewsSummary> findByArticleId(String articleId);

	List<NewsSummary> findByArticleIdIn(List<String> articleIds);
}
