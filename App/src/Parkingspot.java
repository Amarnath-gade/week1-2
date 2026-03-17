import java.util.*;

enum Status {
    EMPTY, OCCUPIED, DELETED
}

class ParkingSpot {
    String licensePlate;
    long entryTime;
    Status status;

    public ParkingSpot() {
        this.status = Status.EMPTY;
    }
}

public class ParkingLot {

    private final ParkingSpot[] table;
    private final int capacity;

    private int occupied = 0;
    private int totalProbes = 0;
    private int operations = 0;

    public ParkingLot(int size) {
        this.capacity = size;
        table = new ParkingSpot[size];

        for (int i = 0; i < size; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public void parkVehicle(String plate) {
        int index = hash(plate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity; // linear probing
            probes++;
        }

        table[index].licensePlate = plate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        occupied++;
        totalProbes += probes;
        operations++;

        System.out.println("Parked " + plate +
                " → Spot #" + index +
                " (" + probes + " probes)");
    }

    // Exit vehicle
    public void exitVehicle(String plate) {
        int index = hash(plate);

        while (table[index].status != Status.EMPTY) {
            if (table[index].status == Status.OCCUPIED &&
                    table[index].licensePlate.equals(plate)) {

                long duration = System.currentTimeMillis() - table[index].entryTime;
                double hours = duration / (1000.0 * 60 * 60);
                double fee = hours * 5; // $5/hour

                table[index].status = Status.DELETED;
                occupied--;

                System.out.printf("Exit %s → Spot #%d freed | Duration: %.2f hrs | Fee: $%.2f\n",
                        plate, index, hours, fee);
                return;
            }

            index = (index + 1) % capacity;
        }

        System.out.println("Vehicle not found");
    }

    // Find nearest available spot
    public int findNearestSpot() {
        for (int i = 0; i < capacity; i++) {
            if (table[i].status != Status.OCCUPIED) {
                return i;
            }
        }
        return -1;
    }

    // Statistics
    public void getStatistics() {
        double occupancyRate = (occupied * 100.0) / capacity;
        double avgProbes = (operations == 0) ? 0 : (totalProbes * 1.0 / operations);

        System.out.printf("Occupancy: %.2f%%\n", occupancyRate);
        System.out.printf("Avg Probes: %.2f\n", avgProbes);
        System.out.println("Peak Hour: 2-3 PM (simulated)");
    }

    // Demo
    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot(10);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        lot.exitVehicle("ABC-1234");

        System.out.println("Nearest Spot: " + lot.findNearestSpot());

        lot.getStatistics();
    }
}