package com.login.gymcrm.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidGenerator {

    public String generate() {
        return UUID.randomUUID().toString();
    }
}
