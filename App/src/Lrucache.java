import java.util.*;

// Video Data Model
class Video {
    String id;
    String content;

    public Video(String id, String content) {
        this.id = id;
        this.content = content;
    }
}

// LRU Cache using LinkedHashMap
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

public class MultiLevelCache {

    // L1, L2, L3
    private final LRUCache<String, Video> L1 = new LRUCache<>(10000);
    private final LRUCache<String, Video> L2 = new LRUCache<>(100000);
    private final Map<String, Video> L3 = new HashMap<>();

    // Access count for promotion
    private final Map<String, Integer> accessCount = new HashMap<>();

    // Stats
    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;

    // 🔹 Get Video
    public Video getVideo(String videoId) {

        long start = System.currentTimeMillis();

        // L1
        if (L1.containsKey(videoId)) {
            L1Hits++;
            System.out.println("L1 HIT (0.5ms)");
            return L1.get(videoId);
        }

        // L2
        if (L2.containsKey(videoId)) {
            L2Hits++;
            System.out.println("L2 HIT (5ms)");

            Video v = L2.get(videoId);

            // Promote to L1
            promoteToL1(videoId, v);

            return v;
        }

        // L3 (DB)
        if (L3.containsKey(videoId)) {
            L3Hits++;
            System.out.println("L3 HIT (150ms)");

            Video v = L3.get(videoId);

            // Add to L2 first
            L2.put(videoId, v);
            accessCount.put(videoId, 1);

            return v;
        }

        System.out.println("Video not found");
        return null;
    }

    // 🔹 Promote to L1
    private void promoteToL1(String videoId, Video v) {
        int count = accessCount.getOrDefault(videoId, 0) + 1;
        accessCount.put(videoId, count);

        if (count >= 2) { // threshold
            L1.put(videoId, v);
        }
    }

    // 🔹 Add video to DB (L3)
    public void addToDatabase(Video v) {
        L3.put(v.id, v);
    }

    // 🔹 Invalidate cache (content update)
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);
        accessCount.remove(videoId);

        System.out.println("Cache invalidated for " + videoId);
    }

    // 🔹 Stats
    public void getStatistics() {
        int total = L1Hits + L2Hits + L3Hits;

        System.out.println("\n===== CACHE STATS =====");

        System.out.printf("L1 Hit Rate: %.2f%%\n", (L1Hits * 100.0) / total);
        System.out.printf("L2 Hit Rate: %.2f%%\n", (L2Hits * 100.0) / total);
        System.out.printf("L3 Hit Rate: %.2f%%\n", (L3Hits * 100.0) / total);

        System.out.println("Avg Times:");
        System.out.println("L1: 0.5ms, L2: 5ms, L3: 150ms");

        double overall = ((L1Hits + L2Hits) * 100.0) / total;
        System.out.printf("Overall Hit Rate: %.2f%%\n", overall);
    }

    // 🔹 Demo
    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        // Add videos to DB
        cache.addToDatabase(new Video("video_123", "Movie A"));
        cache.addToDatabase(new Video("video_999", "Movie B"));

        // First request
        System.out.println("\nRequest 1:");
        cache.getVideo("video_123");

        // Second request (promotion)
        System.out.println("\nRequest 2:");
        cache.getVideo("video_123");

        // Third request (L1 hit)
        System.out.println("\nRequest 3:");
        cache.getVideo("video_123");

        // Another video
        System.out.println("\nRequest 4:");
        cache.getVideo("video_999");

        // Stats
        cache.getStatistics();
    }
}