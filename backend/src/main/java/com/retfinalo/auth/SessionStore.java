package com.retfinalo.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sesiones en memoria: token aleatorio → id de usuario, con vencimiento (7 días). */
public final class SessionStore {
    public static final SessionStore INSTANCE = new SessionStore();
    private static final long TTL_SECONDS = 7L * 24 * 60 * 60;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Entry> sessions = new ConcurrentHashMap<>();

    public String create(long userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Entry(userId, now() + TTL_SECONDS));
        return token;
    }

    public long userId(String token) {
        if (token == null) return -1;
        Entry entry = sessions.get(token);
        if (entry == null) return -1;
        if (entry.expiresAt < now()) {
            sessions.remove(token);
            return -1;
        }
        return entry.userId;
    }

    public void revoke(String token) {
        if (token != null) sessions.remove(token);
    }

    private static long now() {
        return System.currentTimeMillis() / 1000;
    }

    private record Entry(long userId, long expiresAt) {
    }
}