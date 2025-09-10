package com.sum_news_BE.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sum_news_BE.domain.RefreshToken;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, ObjectId> {
	Optional<RefreshToken> findByEmail(String email);
	void deleteByEmail(String email);
}
