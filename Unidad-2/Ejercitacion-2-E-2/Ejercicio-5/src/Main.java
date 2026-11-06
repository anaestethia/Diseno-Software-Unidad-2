import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final int requestCount = 30;
        final ReservationManager manager = ReservationManager.getInstance();
        final CountDownLatch ready = new CountDownLatch(requestCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(requestCount);
        final int[] accepted = {0};
        ExecutorService pool = Executors.newFixedThreadPool(requestCount);

        for (int i = 1; i <= requestCount; i++) {
            final String memberId = "SOCIO-" + i;
            pool.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (manager.reserve(memberId)) {
                        synchronized (accepted) {
                            accepted[0]++;
                        }
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        int active = manager.getReservations().size();
        int available = manager.getAvailableSeats();
        if (accepted[0] != 10 || active != 10 || available != 0 || available < 0) {
            throw new AssertionError("Invariante de capacidad incumplida.");
        }
        System.out.println("Solicitudes concurrentes: " + requestCount);
        System.out.println("Reservas aceptadas: " + accepted[0] + "; rechazadas: " + (requestCount - accepted[0]));
        System.out.println("Reservas activas: " + active + "; lugares disponibles: " + available);
        int beforeInvalidCancellation = manager.getAvailableSeats();
        boolean invalidCancellationAccepted = manager.cancel("SOCIO-999");
        int afterInvalidCancellation = manager.getAvailableSeats();
        if (invalidCancellationAccepted || beforeInvalidCancellation != afterInvalidCancellation) {
            throw new AssertionError("Cancelar una reserva inexistente alteró la disponibilidad.");
        }
        System.out.println("Cancelar socio inexistente: " + invalidCancellationAccepted
                + "; disponibles después: " + afterInvalidCancellation);
        manager.cancel(manager.getReservations().get(0));
        System.out.println("Tras cancelar una reserva válida, disponibles: " + manager.getAvailableSeats());
    }
}

final class ReservationManager {
    private static final int CAPACITY = 10;
    private final Set<String> reservations = new LinkedHashSet<>();

    private ReservationManager() {
    }

    private static class Holder {
        private static final ReservationManager INSTANCE = new ReservationManager();
    }

    static ReservationManager getInstance() {
        return Holder.INSTANCE;
    }

    synchronized boolean reserve(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("El socio es obligatorio.");
        }
        if (reservations.contains(memberId) || reservations.size() >= CAPACITY) {
            return false;
        }
        reservations.add(memberId);
        return true;
    }

    synchronized boolean cancel(String memberId) {
        return reservations.remove(memberId);
    }

    synchronized int getAvailableSeats() {
        return CAPACITY - reservations.size();
    }

    synchronized List<String> getReservations() {
        return Collections.unmodifiableList(new ArrayList<>(reservations));
    }
}
