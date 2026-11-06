import java.util.ArrayDeque;
import java.util.Queue;

public final class PrintQueue {
    private static final PrintQueue INSTANCE = new PrintQueue();

    private final Queue<PrintJob> jobs = new ArrayDeque<>();
    private long nextSequence = 1;
    private int completedJobs;

    private PrintQueue() {
    }

    public static PrintQueue getInstance() {
        return INSTANCE;
    }

    public synchronized long enqueue(String fileName, String author) {
        if (fileName == null || fileName.isBlank()
                || author == null || author.isBlank()) {
            throw new IllegalArgumentException("File name and author must not be blank");
        }
        long sequence = nextSequence++;
        jobs.add(new PrintJob(sequence, fileName, author));
        return sequence;
    }

    public synchronized boolean processNext() {
        PrintJob job = jobs.poll();
        if (job == null) {
            return false;
        }
        System.out.println("Imprimiendo #" + job.sequence + ": " + job.fileName
                + " | autor=" + job.author);
        completedJobs++;
        return true;
    }

    public synchronized int pendingCount() {
        return jobs.size();
    }

    public synchronized int completedCount() {
        return completedJobs;
    }

    private static final class PrintJob {
        private final long sequence;
        private final String fileName;
        private final String author;

        private PrintJob(long sequence, String fileName, String author) {
            this.sequence = sequence;
            this.fileName = fileName;
            this.author = author;
        }
    }
}
