package com.sum_news_BE.repository;

import com.sum_news_BE.domain.NewsSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NewsSummaryRepository extends MongoRepository<NewsSummary, Integer> {
    List<NewsSummary> findByArticleId(Integer articleId);
}
