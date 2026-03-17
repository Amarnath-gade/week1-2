import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TokenBucket {
    private final int maxTokens;
    private final double refillRate; // tokens per second

    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    // Refill tokens based on elapsed time
    private synchronized void refill() {
        long now = System.currentTimeMillis();
        double seconds = (now - lastRefillTime) / 1000.0;

        double tokensToAdd = seconds * refillRate;
        tokens = Math.min(maxTokens, tokens + tokensToAdd);

        lastRefillTime = now;
    }

    // Try to consume a token
    public synchronized boolean allowRequest() {
        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized int getRemainingTokens() {
        refill();
        return (int) tokens;
    }

    public long getRetryAfterSeconds() {
        return (long) ((1 - tokens) / refillRate);
    }
}

public class RateLimiter {

    private final ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 1000;
    private static final double REFILL_RATE = 1000.0 / 3600; // per second

    public String checkRateLimit(String clientId) {

        TokenBucket bucket = clients.computeIfAbsent(
                clientId,
                id -> new TokenBucket(MAX_REQUESTS, REFILL_RATE)
        );

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 remaining, retry after " +
                    bucket.getRetryAfterSeconds() + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) return "No data";

        return "{used: " + (MAX_REQUESTS - bucket.getRemainingTokens()) +
                ", limit: " + MAX_REQUESTS + "}";
    }

    // Demo
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }
    }
}