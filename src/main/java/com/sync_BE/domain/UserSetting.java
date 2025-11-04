package com.sync_BE.domain;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "user_setting")
public class UserSetting {

	@Id
	private ObjectId id;

	@DBRef
	@JsonIgnore
	private User user;

	private boolean ttsEnabled;
	private String ttsVoice;

	private Double pitch;

	private Double speakingRate;

	private LocalDateTime updatedAt;
}
