package com.login.gymcrm.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomPasswordGenerator {
    private static final char[] ALLOWED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private final SecureRandom random = new SecureRandom();

    
    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = ALLOWED[random.nextInt(ALLOWED.length)];
        }
        return new String(result);
    }
}
