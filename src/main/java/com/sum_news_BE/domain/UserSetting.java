package com.sum_news_BE.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Data
@Builder
public class UserSetting {

    private boolean tts_enabled;

    private String tts_voice;

    private LocalDateTime updated_at;

    @DBRef
    private User user;
}
