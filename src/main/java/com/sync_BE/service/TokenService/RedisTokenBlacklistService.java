package com.sync_BE.service.TokenService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisTokenBlacklistService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5);

    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        try {
            redisTemplate.opsForValue().set(key, "1", TOKEN_TTL);
            log.info("토큰이 블랙리스트에 추가됨: {}... (5분 후 자동 삭제)",
                token.substring(0, Math.min(10, token.length())));
        } catch (RedisConnectionFailureException ex) {
            log.error("Redis 연결 실패: 블랙리스트 추가 불가. {}", ex.getMessage());
        } catch (Exception e) {
            log.error("블랙리스트 추가 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (RedisConnectionFailureException ex) {
            log.error("Redis 연결 실패: 블랙리스트 확인 불가. {}", ex.getMessage());
            return false; // Redis 접근 실패 시 false 반환 (JWT만으로 인증 허용)
        } catch (Exception e) {
            log.error("블랙리스트 확인 중 예외 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        try {
            redisTemplate.delete(key);
            log.debug("토큰이 블랙리스트에서 제거됨: {}...",
                token.substring(0, Math.min(10, token.length())));
        } catch (RedisConnectionFailureException ex) {
            log.error("Redis 연결 실패: 블랙리스트 삭제 불가. {}", ex.getMessage());
        } catch (Exception e) {
            log.error("블랙리스트 삭제 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
