package com.login.gymcrm.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class UsernameGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    public String generate(String firstName, String lastName) {
        String base = normalizeName(firstName) + "." + normalizeName(lastName);

        List<String> usernames = entityManager.createQuery("select u.username from User u", String.class)
                .getResultList();

        boolean baseTaken = usernames.stream().anyMatch(u -> u.equalsIgnoreCase(base));
        int maxSuffix = usernames.stream().mapToInt(u -> parseSuffix(base, u)).max().orElse(0);

        if (!baseTaken && maxSuffix == 0) {
            return base;
        }
        return base + (maxSuffix + 1);
    }

    private int parseSuffix(String base, String username) {
        String lowerBase = base.toLowerCase(Locale.US);
        String lowerUser = username.toLowerCase(Locale.US);
        if (!lowerUser.startsWith(lowerBase) || lowerUser.length() == lowerBase.length()) {
            return 0;
        }
        String suffix = username.substring(base.length());
        if (!suffix.chars().allMatch(Character::isDigit)) {
            return 0;
        }
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalizeName(String name) {
        String trimmed = name == null ? "" : name.trim();
        return trimmed.replaceAll("\\s+", "");
    }
}
