package com.sum_news_BE.repository;

import java.util.List;

import com.sum_news_BE.domain.NewsArticle;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsArticleRepository extends MongoRepository<NewsArticle, ObjectId> {
	List<NewsArticle> findAllByOrderByCreatedAtDesc();
}
