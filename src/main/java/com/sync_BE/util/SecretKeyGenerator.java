package com.sync_BE.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        String secretKey = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Generated JWT Secret Key: " + secretKey);
    }
} 