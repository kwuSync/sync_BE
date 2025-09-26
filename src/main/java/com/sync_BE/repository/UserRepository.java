package com.sync_BE.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sync_BE.domain.User;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByNickname(String nickname);
}
