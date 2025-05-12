package com.sum_news_BE.repository;

import java.util.Optional;

import com.sum_news_BE.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {

	Optional<User> findByUserid(String userid);
}
