import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        ActivityLog loans = ActivityLog.getInstance();
        ActivityLog returns = ActivityLog.getInstance();
        ActivityLog service = ActivityLog.getInstance();

        loans.record("Préstamos", "Préstamo del libro L-10 a socio S-1");
        returns.record("Devoluciones", "Devolución del libro L-08");
        service.record("Atención", "Alta del socio S-2");

        System.out.println("Historial en orden:");
        for (Activity activity : service.getHistory()) {
            System.out.println(activity);
        }
        System.out.println("Eventos de Préstamos: " + loans.bySector("Préstamos").size());
        System.out.println("Total de eventos: " + service.count());
        System.out.println("Mismo registro en los tres sectores: "
                + (loans == returns && returns == service));
        try {
            service.getHistory().clear();
            throw new AssertionError("El historial no debe ser modificable.");
        } catch (UnsupportedOperationException expected) {
            System.out.println("Historial protegido contra modificaciones externas: sí.");
        }
    }
}

final class Activity {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String sector;
    private final String description;

    Activity(String sector, String description) {
        this.sector = sector;
        this.description = description;
    }

    String getSector() {
        return sector;
    }

    @Override
    public String toString() {
        return FORMAT.format(timestamp) + " | " + sector + " | " + description;
    }
}

final class ActivityLog {
    private final List<Activity> activities = new ArrayList<>();

    private ActivityLog() {
    }

    private static class Holder {
        private static final ActivityLog INSTANCE = new ActivityLog();
    }

    static ActivityLog getInstance() {
        return Holder.INSTANCE;
    }

    synchronized void record(String sector, String description) {
        if (sector == null || sector.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Sector y descripción son obligatorios.");
        }
        activities.add(new Activity(sector.trim(), description.trim()));
    }

    synchronized List<Activity> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(activities));
    }

    synchronized List<Activity> bySector(String sector) {
        return activities.stream().filter(a -> a.getSector().equalsIgnoreCase(sector))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    synchronized int count() {
        return activities.size();
    }
}
