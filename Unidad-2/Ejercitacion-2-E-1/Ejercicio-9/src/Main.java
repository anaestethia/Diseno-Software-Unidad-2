import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PerformanceMetrics metrics = PerformanceMetrics.getInstance();
        System.out.println("Singleton compartido: "
                + (metrics == PerformanceMetrics.getInstance()));

        int threadCount = 8;
        int operationsPerThread = 2500;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int thread = 0; thread < threadCount; thread++) {
            final int threadId = thread;
            new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < operationsPerThread; i++) {
                        String operation = "endpoint-" + ((i + threadId) % 12);
                        metrics.record(operation, 100 + threadId);
                    }
                } catch (Throwable exception) {
                    failure.compareAndSet(null, exception);
                } finally {
                    done.countDown();
                }
            }, "Metrica-" + thread).start();
        }
        start.countDown();
        done.await();
        if (failure.get() != null) {
            throw new IllegalStateException("Concurrent metric collection failed", failure.get());
        }

        Map<String, PerformanceMetrics.MetricSnapshot> summary = metrics.exportSummary();
        System.out.println("Operaciones registradas: " + metrics.totalOperations()
                + " (esperadas: " + (threadCount * operationsPerThread) + ")");
        System.out.println("Categorias almacenadas: " + summary.size()
                + " (capacidad maxima: " + metrics.metricCapacity() + ")");
        System.out.println("Agregado de nombres adicionales: " + summary.get("_other_"));
        System.out.println("Resumen inmutable: " + isUnmodifiable(summary));
    }

    private static boolean isUnmodifiable(
            Map<String, PerformanceMetrics.MetricSnapshot> summary) {
        try {
            summary.clear();
            return false;
        } catch (UnsupportedOperationException expected) {
            return true;
        }
    }
}
