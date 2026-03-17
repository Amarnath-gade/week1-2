import java.util.*;

public class PlagiarismDetector {

    // n-gram → set of document IDs
    private final HashMap<String, Set<Integer>> index = new HashMap<>();

    // document ID → list of n-grams
    private final HashMap<Integer, List<String>> docNgrams = new HashMap<>();

    private final int N = 5; // 5-gram

    // Add document to database
    public void addDocument(int docId, String text) {
        List<String> ngrams = generateNgrams(text);
        docNgrams.put(docId, ngrams);

        for (String gram : ngrams) {
            index.computeIfAbsent(gram, k -> new HashSet<>()).add(docId);
        }
    }

    // Analyze a new document
    public void analyzeDocument(int docId, String text) {
        List<String> ngrams = generateNgrams(text);
        HashMap<Integer, Integer> matchCount = new HashMap<>();

        for (String gram : ngrams) {
            if (index.containsKey(gram)) {
                for (int existingDoc : index.get(gram)) {
                    matchCount.put(existingDoc,
                            matchCount.getOrDefault(existingDoc, 0) + 1);
                }
            }
        }

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        for (Map.Entry<Integer, Integer> entry : matchCount.entrySet()) {
            int existingDoc = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println("Matched with Doc " + existingDoc +
                    " → " + matches + " matches → Similarity: " +
                    String.format("%.2f", similarity) + "%");

            if (similarity > 60) {
                System.out.println("⚠️ PLAGIARISM DETECTED!");
            }
        }
    }

    // Generate n-grams
    private List<String> generateNgrams(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }
            ngrams.add(gram.toString().trim());
        }

        return ngrams;
    }

    // Demo
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        detector.addDocument(1, "AI is transforming the world with new innovations and technologies");
        detector.addDocument(2, "Machine learning and AI are transforming the world rapidly");

        detector.analyzeDocument(3,
                "AI is transforming the world with new technologies and innovations");

    }
}