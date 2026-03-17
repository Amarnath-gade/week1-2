import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

 class UsernameChecker {

    // Thread-safe maps for concurrency
    private ConcurrentHashMap<String, Integer> userMap;
    private ConcurrentHashMap<String, Integer> attemptCount;

    public UsernameChecker() {
        userMap = new ConcurrentHashMap<>();
        attemptCount = new ConcurrentHashMap<>();
    }

    // Add a user (simulate registration)
    public void registerUser(String username, int userId) {
        userMap.put(username, userId);
    }

    // Check username availability (O(1))
    public boolean checkAvailability(String username) {
        attemptCount.put(username, attemptCount.getOrDefault(username, 0) + 1);
        return !userMap.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        // Strategy 1: Add numbers
        for (int i = 1; i <= 5; i++) {
            String newName = username + i;
            if (!userMap.containsKey(newName)) {
                suggestions.add(newName);
            }
        }

        // Strategy 2: Replace characters
        if (username.contains("_")) {
            String modified = username.replace("_", ".");
            if (!userMap.containsKey(modified)) {
                suggestions.add(modified);
            }
        }

        // Strategy 3: Add prefix
        String prefix = "real_" + username;
        if (!userMap.containsKey(prefix)) {
            suggestions.add(prefix);
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String maxUser = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxUser = entry.getKey();
            }
        }

        return maxUser + " (" + maxCount + " attempts)";
    }

    // Display all users (for testing)
    public void displayUsers() {
        System.out.println("Registered Users: " + userMap.keySet());
    }

    // Main method to test system
    public static void main(String[] args) {

        UsernameChecker system = new UsernameChecker();

        // Simulate existing users
        system.registerUser("john_doe", 101);
        system.registerUser("admin", 102);
        system.registerUser("user123", 103);

        system.displayUsers();

        // Check availability
        String username1 = "john_doe";
        String username2 = "jane_smith";

        System.out.println("\nChecking availability:");

        System.out.println(username1 + " → " + system.checkAvailability(username1));
        System.out.println(username2 + " → " + system.checkAvailability(username2));

        // Suggest alternatives
        System.out.println("\nSuggestions for " + username1 + ":");
        List<String> suggestions = system.suggestAlternatives(username1);
        for (String s : suggestions) {
            System.out.println(s);
        }

        // Simulate multiple attempts
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("admin");

        // Get most attempted
        System.out.println("\nMost attempted username:");
        System.out.println(system.getMostAttempted());
    }
}
