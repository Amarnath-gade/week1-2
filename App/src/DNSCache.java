import java.util.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime;

    public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class DNSCache {

    private final int capacity;
    private final LinkedHashMap<String, DNSEntry> cache;

    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        // LRU Cache using LinkedHashMap
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > capacity;
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                long time = (System.nanoTime() - startTime) / 1_000_000;
                return "Cache HIT → " + entry.ipAddress + " (" + time + " ms)";
            } else {
                cache.remove(domain); // expired
            }
        }

        // Cache MISS → simulate upstream DNS
        misses++;
        DNSEntry newEntry = queryUpstreamDNS(domain);
        cache.put(domain, newEntry);

        long time = (System.nanoTime() - startTime) / 1_000_000;
        return "Cache MISS → " + newEntry.ipAddress + " (" + time + " ms)";
    }

    // Simulated upstream DNS call
    private DNSEntry queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate latency (100ms)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String fakeIP = "172.217." + (int)(Math.random() * 255) + "." + (int)(Math.random() * 255);
        return new DNSEntry(domain, fakeIP, 5); // TTL = 5 seconds
    }

    // Cleanup expired entries periodically
    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000); // every 2 sec
                    synchronized (this) {
                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                        while (it.hasNext()) {
                            if (it.next().getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }

    // Cache statistics
    public String getCacheStats() {
        int total = hits + misses;
        double hitRate = (total == 0) ? 0 : (hits * 100.0 / total);
        return "Hit Rate: " + String.format("%.2f", hitRate) + "% | Hits: " + hits + " | Misses: " + misses;
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache cache = new DNSCache(3);

        System.out.println(cache.resolve("google.com")); // MISS
        System.out.println(cache.resolve("google.com")); // HIT

        Thread.sleep(6000); // wait for TTL expiry

        System.out.println(cache.resolve("google.com")); // EXPIRED → MISS
        System.out.println(cache.getCacheStats());
    }
}