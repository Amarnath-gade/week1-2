import java.util.*;
import java.util.concurrent.*;

class Event {
    String url, userId, source;

    public Event(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

public class AnalyticsDashboard {

    private final ConcurrentHashMap<String, Integer> pageViews = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> uniqueUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> sourceCount = new ConcurrentHashMap<>();

    // Process event in O(1)
    public void processEvent(Event e) {

        // Page views
        pageViews.merge(e.url, 1, Integer::sum);

        // Unique users
        uniqueUsers.computeIfAbsent(e.url, k -> ConcurrentHashMap.newKeySet())
                .add(e.userId);

        // Traffic sources
        sourceCount.merge(e.source, 1, Integer::sum);
    }

    // Get Top 10 Pages
    public List<String> getTopPages() {

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            Map.Entry<String, Integer> e = pq.poll();
            int unique = uniqueUsers.getOrDefault(e.getKey(), Set.of()).size();

            result.add(e.getKey() + " - " + e.getValue() +
                    " views (" + unique + " unique)");
        }

        Collections.reverse(result);
        return result;
    }

    // Traffic percentage
    public void printTrafficSources() {
        int total = sourceCount.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> e : sourceCount.entrySet()) {
            double percent = (e.getValue() * 100.0) / total;
            System.out.printf("%s: %.1f%%\n", e.getKey(), percent);
        }
    }

    // Dashboard
    public void showDashboard() {
        System.out.println("\n===== LIVE DASHBOARD =====");

        System.out.println("\nTop Pages:");
        int i = 1;
        for (String page : getTopPages()) {
            System.out.println(i++ + ". " + page);
        }

        System.out.println("\nTraffic Sources:");
        printTrafficSources();
    }

    // Auto refresh every 5 sec
    public void startDashboard() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::showDashboard, 0, 5, TimeUnit.SECONDS);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        AnalyticsDashboard system = new AnalyticsDashboard();
        system.startDashboard();

        String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/ai"};
        String[] sources = {"Google", "Facebook", "Direct"};

        Random r = new Random();

        for (int i = 0; i < 100; i++) {
            system.processEvent(new Event(
                    urls[r.nextInt(urls.length)],
                    "user_" + r.nextInt(50),
                    sources[r.nextInt(sources.length)]
            ));
            Thread.sleep(100);
        }
    }
}