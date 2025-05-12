package com.sum_news_BE.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sum_news_BE.domain.RefreshToken;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
	Optional<RefreshToken> findByUserid(String userid);
	void deleteByUserid(String userid);
}
