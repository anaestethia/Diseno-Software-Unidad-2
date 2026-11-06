import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicInteger;

public final class PerformanceMetrics {
    private static final int MAX_NAMED_OPERATIONS = 8;
    private static final String OTHER_OPERATION = "_other_";

    private final ConcurrentMap<String, Metric> metrics = new ConcurrentHashMap<>();
    private final AtomicInteger namedOperationCount = new AtomicInteger();
    private final LongAdder totalOperations = new LongAdder();

    private PerformanceMetrics() {
        metrics.put(OTHER_OPERATION, new Metric());
    }

    private static class Holder {
        private static final PerformanceMetrics INSTANCE = new PerformanceMetrics();
    }

    public static PerformanceMetrics getInstance() {
        return Holder.INSTANCE;
    }

    public void record(String operationName, long latencyNanos) {
        if (operationName == null || operationName.isBlank()
                || latencyNanos < 0) {
            throw new IllegalArgumentException("A name and non-negative latency are required");
        }
        String normalizedName = operationName.trim();
        if (OTHER_OPERATION.equals(normalizedName)) {
            throw new IllegalArgumentException("The overflow metric name is reserved");
        }
        Metric metric = metrics.get(normalizedName);
        if (metric == null) {
            metric = registerOrUseOther(normalizedName);
        }
        metric.record(latencyNanos);
        totalOperations.increment();
    }

    public Map<String, MetricSnapshot> exportSummary() {
        Map<String, MetricSnapshot> snapshot = new TreeMap<>();
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().snapshot());
        }
        return Collections.unmodifiableMap(snapshot);
    }

    public long totalOperations() {
        return totalOperations.sum();
    }

    public int metricCapacity() {
        return MAX_NAMED_OPERATIONS + 1;
    }

    private Metric registerOrUseOther(String name) {
        while (true) {
            int current = namedOperationCount.get();
            if (current >= MAX_NAMED_OPERATIONS) {
                return metrics.get(OTHER_OPERATION);
            }
            if (namedOperationCount.compareAndSet(current, current + 1)) {
                Metric candidate = new Metric();
                Metric existing = metrics.putIfAbsent(name, candidate);
                if (existing != null) {
                    namedOperationCount.decrementAndGet();
                    return existing;
                }
                return candidate;
            }
        }
    }

    public static final class MetricSnapshot {
        private final long count;
        private final long totalLatencyNanos;

        private MetricSnapshot(long count, long totalLatencyNanos) {
            this.count = count;
            this.totalLatencyNanos = totalLatencyNanos;
        }

        public long getCount() {
            return count;
        }

        public long getTotalLatencyNanos() {
            return totalLatencyNanos;
        }

        public double getAverageLatencyNanos() {
            return count == 0 ? 0.0 : (double) totalLatencyNanos / count;
        }

        @Override
        public String toString() {
            return "count=" + count + ", totalLatencyNanos=" + totalLatencyNanos
                    + ", averageLatencyNanos=" + String.format("%.1f", getAverageLatencyNanos());
        }
    }

    private static final class Metric {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalLatencyNanos = new LongAdder();

        private void record(long latencyNanos) {
            count.increment();
            totalLatencyNanos.add(latencyNanos);
        }

        private MetricSnapshot snapshot() {
            return new MetricSnapshot(count.sum(), totalLatencyNanos.sum());
        }
    }
}
