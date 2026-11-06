import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PrintQueue queue = PrintQueue.getInstance();
        System.out.println("Singleton compartido: " + (queue == PrintQueue.getInstance()));

        int producerCount = 6;
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> producers = new ArrayList<>();
        for (int i = 1; i <= producerCount; i++) {
            final int id = i;
            Thread producer = new Thread(() -> {
                try {
                    start.await();
                    queue.enqueue("documento-" + id + ".pdf", "usuario-" + id);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }, "Terminal-" + i);
            producers.add(producer);
            producer.start();
        }

        start.countDown();
        for (Thread producer : producers) {
            producer.join();
        }
        System.out.println("Trabajos encolados: " + queue.pendingCount());
        while (queue.processNext()) {
            // El siguiente trabajo se toma únicamente tras completar el anterior.
        }
        System.out.println("Completados: " + queue.completedCount()
                + "; pendientes: " + queue.pendingCount());
    }
}
