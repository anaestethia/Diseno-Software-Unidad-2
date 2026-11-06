import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        SecurityAuditLog audit = SecurityAuditLog.INSTANCE;
        System.out.println("Singleton compartido: " + (audit == SecurityAuditLog.INSTANCE));

        int threadCount = 4;
        int eventsPerThread = 25;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int thread = 1; thread <= threadCount; thread++) {
            final int operatorId = thread;
            new Thread(() -> {
                try {
                    start.await();
                    for (int i = 1; i <= eventsPerThread; i++) {
                        audit.record("sesion-" + operatorId, "operador-" + operatorId,
                                "cambio-privilegio-" + i);
                    }
                } catch (Throwable exception) {
                    failure.compareAndSet(null, exception);
                } finally {
                    done.countDown();
                }
            }, "Auditor-" + thread).start();
        }
        start.countDown();
        done.await();
        if (failure.get() != null) {
            throw new IllegalStateException("Concurrent audit recording failed", failure.get());
        }

        List<SecurityAuditLog.AuditEvent> trace = audit.fullTrace();
        boolean strictOrder = hasStrictOrder(trace);
        System.out.println("Registros almacenados: " + audit.count()
                + "; registros consultados: " + trace.size());
        System.out.println("Secuencia y marcas temporales estrictas: " + strictOrder);
        System.out.println("Traza inmutable: " + isUnmodifiable(trace));
        System.out.println("Primero: " + trace.get(0));
        System.out.println("Ultimo: " + trace.get(trace.size() - 1));
    }

    private static boolean hasStrictOrder(List<SecurityAuditLog.AuditEvent> trace) {
        for (int i = 0; i < trace.size(); i++) {
            SecurityAuditLog.AuditEvent event = trace.get(i);
            if (event.getSequence() != i + 1L) {
                return false;
            }
            if (i > 0 && !event.getTimestamp().isAfter(trace.get(i - 1).getTimestamp())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isUnmodifiable(List<SecurityAuditLog.AuditEvent> trace) {
        try {
            trace.clear();
            return false;
        } catch (UnsupportedOperationException expected) {
            return true;
        }
    }
}
