package skyvangaurd.subscription.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class JwtTokenBlacklistService {
  private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryDate) {
        tokenBlacklist.put(token, expiryDate);
    }

    public boolean isTokenBlacklisted(String token) {
        Long expiryDate = tokenBlacklist.get(token);
        if (expiryDate == null) {
            return false;
        }
        // Remove expired tokens
        if (expiryDate < Instant.now().toEpochMilli()) {
            tokenBlacklist.remove(token);
            return false;
        }
        return true;
    }
}
