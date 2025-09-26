package com.sync_BE.service.TokenService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisTokenBlacklistService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5); // 5분 후 자동 삭제
    
    // 토큰을 블랙리스트에 추가
    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", TOKEN_TTL);
        log.info("토큰이 블랙리스트에 추가됨: {}... (5분 후 자동 삭제)", 
                token.substring(0, Math.min(10, token.length())));
    }
    
    // 토큰이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    // 토큰을 블랙리스트에서 제거 (필요시)
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("토큰이 블랙리스트에서 제거됨: {}...", 
                token.substring(0, Math.min(10, token.length())));
    }
}
