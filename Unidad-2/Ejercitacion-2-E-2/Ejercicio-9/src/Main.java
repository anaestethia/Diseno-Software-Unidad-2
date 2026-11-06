import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final int threadCount = 10;
        final int documentsPerThread = 3;
        final int total = threadCount * documentsPerThread;
        final PrintManager[] instances = new PrintManager[threadCount];
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int thread = i;
            pool.execute(() -> {
                instances[thread] = PrintManager.getInstance();
                ready.countDown();
                try {
                    start.await();
                    String[] sectors = {"Recepción", "Facturación", "Archivo"};
                    for (int n = 0; n < documentsPerThread; n++) {
                        String id = "DOC-" + thread + "-" + n;
                        instances[thread].enqueue(new PrintJob(id, "Informe " + id,
                                sectors[(thread + n) % sectors.length], n + 1));
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

        boolean sameInstance = true;
        for (PrintManager instance : instances) {
            sameInstance &= instance == instances[0];
        }
        List<PrintJob> processed = instances[0].processAll();
        Set<String> uniqueIds = new HashSet<>();
        boolean sequential = true;
        for (int i = 0; i < processed.size(); i++) {
            uniqueIds.add(processed.get(i).id);
            sequential &= processed.get(i).sequence == i + 1;
        }
        if (processed.size() != total || uniqueIds.size() != total || !sequential || !sameInstance) {
            throw new AssertionError("La cola perdió, duplicó o desordenó trabajos.");
        }
        System.out.println("Hilos remitentes: " + threadCount + "; documentos encolados: " + total);
        System.out.println("Documentos procesados exactamente una vez: " + uniqueIds.size());
        System.out.println("Orden FIFO por secuencia de incorporación: " + sequential);
        System.out.println("Todos los hilos usaron el mismo gestor: " + sameInstance);
        System.out.println("Ejemplo de informe: " + processed.get(0));
    }
}

final class PrintJob {
    final String id;
    final String name;
    final String sector;
    final int pages;
    final int sequence;

    PrintJob(String id, String name, String sector, int pages) {
        this(id, name, sector, pages, 0);
    }

    PrintJob(String id, String name, String sector, int pages, int sequence) {
        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty()
                || sector == null || sector.trim().isEmpty() || pages <= 0) {
            throw new IllegalArgumentException("Trabajo de impresión inválido.");
        }
        this.id = id;
        this.name = name;
        this.sector = sector;
        this.pages = pages;
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "#" + sequence + " " + id + " | " + name + " | " + sector + " | " + pages + " pág.";
    }
}

final class PrintManager {
    private final Deque<PrintJob> queue = new ArrayDeque<>();
    private final List<PrintJob> processed = new ArrayList<>();
    private final Set<String> ids = new HashSet<>();
    private int nextSequence;

    private PrintManager() {
    }

    private static class Holder {
        private static final PrintManager INSTANCE = new PrintManager();
    }

    static PrintManager getInstance() {
        return Holder.INSTANCE;
    }

    synchronized void enqueue(PrintJob job) {
        if (!ids.add(job.id)) {
            throw new IllegalArgumentException("Identificador de documento duplicado: " + job.id);
        }
        queue.addLast(new PrintJob(job.id, job.name, job.sector, job.pages, ++nextSequence));
    }

    synchronized List<PrintJob> processAll() {
        while (!queue.isEmpty()) {
            PrintJob job = queue.removeFirst();
            processed.add(job);
            System.out.println("Imprimiendo " + job);
        }
        return Collections.unmodifiableList(new ArrayList<>(processed));
    }
}
