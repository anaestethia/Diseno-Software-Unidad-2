import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public enum SecurityAuditLog {
    INSTANCE;

    private final List<AuditEvent> events = new ArrayList<>();
    private Instant lastTimestamp;

    public synchronized AuditEvent record(
            String sessionId, String operator, String action) {
        validate(sessionId, "session ID");
        validate(operator, "operator");
        validate(action, "action");

        Instant timestamp = Instant.now();
        if (lastTimestamp != null && !timestamp.isAfter(lastTimestamp)) {
            timestamp = lastTimestamp.plusNanos(1);
        }
        AuditEvent event = new AuditEvent(events.size() + 1L, timestamp,
                sessionId, operator, action);
        events.add(event);
        lastTimestamp = timestamp;
        return event;
    }

    public synchronized int count() {
        return events.size();
    }

    public synchronized List<AuditEvent> fullTrace() {
        return List.copyOf(events);
    }

    private static void validate(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    public static final class AuditEvent {
        private final long sequence;
        private final Instant timestamp;
        private final String sessionId;
        private final String operator;
        private final String action;

        private AuditEvent(long sequence, Instant timestamp, String sessionId,
                          String operator, String action) {
            this.sequence = sequence;
            this.timestamp = timestamp;
            this.sessionId = sessionId;
            this.operator = operator;
            this.action = action;
        }

        public long getSequence() {
            return sequence;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getOperator() {
            return operator;
        }

        public String getAction() {
            return action;
        }

        @Override
        public String toString() {
            return "#" + sequence + " " + timestamp + " session=" + sessionId
                    + " operator=" + operator + " action=" + action;
        }
    }
}
