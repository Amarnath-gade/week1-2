import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleManager {

    // Stores product stock
    private final ConcurrentHashMap<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    // Waiting list (FIFO)
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> waitingList = new ConcurrentHashMap<>();

    // Initialize product stock
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check stock (O(1))
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return (stock != null) ? stock.get() : 0;
    }

    // Purchase item
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        // Atomic decrement logic
        while (true) {
            int currentStock = stock.get();

            if (currentStock <= 0) {
                // Add to waiting list
                ConcurrentLinkedQueue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Out of stock. Added to waiting list. Position #" + queue.size();
            }

            // Try to decrement safely
            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success! Remaining stock: " + (currentStock - 1);
            }
        }
    }

    // Serve next customer from waiting list (when stock is restocked)
    public String restock(String productId, int quantity) {
        AtomicInteger stock = stockMap.get(productId);
        ConcurrentLinkedQueue<Integer> queue = waitingList.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        stock.addAndGet(quantity);

        StringBuilder result = new StringBuilder("Restocked. Serving waiting list:\n");

        while (stock.get() > 0 && !queue.isEmpty()) {
            int user = queue.poll();
            stock.decrementAndGet();
            result.append("User ").append(user).append(" got the product\n");
        }

        return result.toString();
    }

    // Demo main method
    public static void main(String[] args) {
        FlashSaleManager manager = new FlashSaleManager();

        manager.addProduct("IPHONE15_256GB", 3);

        System.out.println("Stock: " + manager.checkStock("IPHONE15_256GB"));

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 101));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 102));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 103));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 104)); // waiting

        System.out.println(manager.restock("IPHONE15_256GB", 2));
    }
}