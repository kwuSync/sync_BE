package com.sync_BE.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sync_BE.domain.UserSetting;

@Repository
public interface UserSettingRepository extends MongoRepository<UserSetting, ObjectId> {
	Optional<UserSetting> findByUserId(ObjectId userId);
}
