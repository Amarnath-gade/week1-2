import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    List<String> queries = new ArrayList<>(); // store matching queries
}

public class AutocompleteSystem {

    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> frequencyMap = new HashMap<>();
    private final Map<String, List<String>> cache = new HashMap<>();

    // Insert query into Trie
    public void insert(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
            node.queries.add(query);
        }
    }

    // Get suggestions
    public List<String> search(String prefix) {

        // Check cache
        if (cache.containsKey(prefix)) {
            return cache.get(prefix);
        }

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        // Min heap for top 10
        PriorityQueue<String> pq = new PriorityQueue<>(
                Comparator.comparingInt(frequencyMap::get)
        );

        for (String q : node.queries) {
            pq.offer(q);
            if (pq.size() > 10) {
                pq.poll();
            }
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll());
        }

        Collections.reverse(result);
        cache.put(prefix, result);

        return result;
    }

    // Update frequency (new search)
    public void updateFrequency(String query) {
        insert(query);
        cache.clear(); // invalidate cache
    }

    // Demo
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");

        System.out.println(system.search("jav"));

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println(system.search("java"));
    }
}
