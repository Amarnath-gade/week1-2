import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long time;

    public Transaction(int id, int amount, String merchant, String account, long time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Txn{id=" + id + ", amount=" + amount + ", merchant=" + merchant + "}";
    }
}

public class TransactionAnalyzer {

    // 🔹 1. Classic Two-Sum
    public static List<String> findTwoSum(List<Transaction> txns, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : txns) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                Transaction prev = map.get(complement);
                result.add("(" + prev.id + ", " + t.id + ")");
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // 🔹 2. Two-Sum with Time Window (1 hour = 3600 sec)
    public static List<String> findTwoSumWithWindow(List<Transaction> txns, int target) {
        Map<Integer, List<Transaction>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : txns) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                for (Transaction prev : map.get(complement)) {
                    if (Math.abs(t.time - prev.time) <= 3600) {
                        result.add("(" + prev.id + ", " + t.id + ")");
                    }
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // 🔹 3. Duplicate Detection
    public static List<String> detectDuplicates(List<Transaction> txns) {
        Map<String, List<Transaction>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : txns) {
            String key = t.amount + "-" + t.merchant;

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<String, List<Transaction>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add("Duplicate: " + entry.getKey() +
                        " → " + entry.getValue());
            }
        }

        return result;
    }

    // 🔹 4. K-Sum (Generalized)
    public static List<List<Integer>> findKSum(List<Transaction> txns, int k, int target) {
        List<Integer> amounts = new ArrayList<>();
        for (Transaction t : txns) {
            amounts.add(t.amount);
        }

        List<List<Integer>> result = new ArrayList<>();
        kSum(amounts, target, k, 0, new ArrayList<>(), result);
        return result;
    }

    private static void kSum(List<Integer> nums, int target, int k, int start,
                             List<Integer> path, List<List<Integer>> result) {

        if (k == 2) {
            Set<Integer> set = new HashSet<>();

            for (int i = start; i < nums.size(); i++) {
                int complement = target - nums.get(i);

                if (set.contains(complement)) {
                    List<Integer> temp = new ArrayList<>(path);
                    temp.add(nums.get(i));
                    temp.add(complement);
                    result.add(temp);
                }
                set.add(nums.get(i));
            }
            return;
        }

        for (int i = start; i < nums.size(); i++) {
            path.add(nums.get(i));
            kSum(nums, target - nums.get(i), k - 1, i + 1, path, result);
            path.remove(path.size() - 1);
        }
    }

    // 🔹 MAIN METHOD (Demo)
    public static void main(String[] args) {

        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", 1000),
                new Transaction(2, 300, "Store B", "acc2", 1100),
                new Transaction(3, 200, "Store C", "acc3", 1200),
                new Transaction(4, 500, "Store A", "acc4", 1300) // duplicate
        );

        System.out.println("🔹 Two-Sum:");
        System.out.println(findTwoSum(transactions, 500));

        System.out.println("\n🔹 Two-Sum (Time Window):");
        System.out.println(findTwoSumWithWindow(transactions, 500));

        System.out.println("\n🔹 Duplicates:");
        System.out.println(detectDuplicates(transactions));

        System.out.println("\n🔹 K-Sum (k=3, target=1000):");
        System.out.println(findKSum(transactions, 3, 1000));
    }
}