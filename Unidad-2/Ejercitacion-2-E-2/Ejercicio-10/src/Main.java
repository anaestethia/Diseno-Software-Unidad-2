import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final AppointmentManager manager = AppointmentManager.getInstance();
        final LocalDate date = LocalDate.of(2026, 11, 12);
        final String professional = "Dra. Vega";
        final String time = "10:30";
        manager.addAvailableSlot(professional, date, time);

        AppointmentManager reception = AppointmentManager.getInstance();
        AppointmentManager selfService = AppointmentManager.getInstance();
        System.out.println("Horario disponible en recepción: "
                + reception.availableSlots(professional, date));
        System.out.println("Horario disponible en autoservicio: "
                + selfService.availableSlots(professional, date));

        final int attempts = 20;
        final CountDownLatch ready = new CountDownLatch(attempts);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(attempts);
        final int[] accepted = {0};
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        for (int i = 1; i <= attempts; i++) {
            final String patient = "PACIENTE-" + i;
            pool.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (AppointmentManager.getInstance().reserve(patient, professional, date, time)) {
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

        if (accepted[0] != 1 || manager.availableSlots(professional, date).contains(time)) {
            throw new AssertionError("El horario fue asignado más de una vez o no fue reservado.");
        }
        System.out.println("Hilos que solicitaron el mismo turno: " + attempts);
        System.out.println("Reservas exitosas: " + accepted[0] + "; ocupadas rechazadas: " + (attempts - 1));
        List<Appointment> appointments = manager.appointmentsFor(professional, date);
        System.out.println("Paciente con turno: " + appointments.get(0).patient);
        int beforeInvalidCancellation = manager.appointmentsFor(professional, date).size();
        boolean invalidCancellationAccepted =
                manager.cancel("PACIENTE-INEXISTENTE", professional, date, time);
        int afterInvalidCancellation = manager.appointmentsFor(professional, date).size();
        if (invalidCancellationAccepted || beforeInvalidCancellation != afterInvalidCancellation) {
            throw new AssertionError("Cancelar un turno inexistente alteró la agenda.");
        }
        System.out.println("Cancelar turno inexistente: " + invalidCancellationAccepted);
        System.out.println("Turnos activos tras cancelación inexistente: "
                + afterInvalidCancellation + " (antes " + beforeInvalidCancellation + ")");
        System.out.println("Misma agenda en recepción y autoservicio: " + (reception == selfService));
        manager.cancel(appointments.get(0).patient, professional, date, time);
        System.out.println("Horario liberado tras cancelar turno válido: "
                + manager.availableSlots(professional, date).contains(time));
    }
}

final class Appointment {
    final String patient;
    final String professional;
    final LocalDate date;
    final String time;

    Appointment(String patient, String professional, LocalDate date, String time) {
        this.patient = patient;
        this.professional = professional;
        this.date = date;
        this.time = time;
    }

    @Override
    public String toString() {
        return patient + " | " + professional + " | " + date + " " + time;
    }
}

final class AppointmentManager {
    private final Map<String, Set<String>> available = new LinkedHashMap<>();
    private final Map<String, Appointment> booked = new LinkedHashMap<>();

    private AppointmentManager() {
    }

    private static class Holder {
        private static final AppointmentManager INSTANCE = new AppointmentManager();
    }

    static AppointmentManager getInstance() {
        return Holder.INSTANCE;
    }

    private String agendaKey(String professional, LocalDate date) {
        return professional + "|" + date;
    }

    private String slotKey(String professional, LocalDate date, String time) {
        return agendaKey(professional, date) + "|" + time;
    }

    synchronized void addAvailableSlot(String professional, LocalDate date, String time) {
        if (professional == null || professional.trim().isEmpty() || date == null
                || time == null || time.trim().isEmpty()) {
            throw new IllegalArgumentException("Profesional, fecha y horario son obligatorios.");
        }
        available.computeIfAbsent(agendaKey(professional, date), key -> new LinkedHashSet<>()).add(time);
    }

    synchronized boolean reserve(String patient, String professional, LocalDate date, String time) {
        if (patient == null || patient.trim().isEmpty() || professional == null || professional.trim().isEmpty()
                || date == null || time == null || time.trim().isEmpty()) {
            throw new IllegalArgumentException("Paciente, profesional, fecha y horario son obligatorios.");
        }
        String slot = slotKey(professional, date, time);
        Set<String> agenda = available.get(agendaKey(professional, date));
        if (agenda == null || booked.containsKey(slot) || !agenda.contains(time)) {
            return false;
        }
        agenda.remove(time);
        booked.put(slot, new Appointment(patient, professional, date, time));
        return true;
    }

    synchronized boolean cancel(String patient, String professional, LocalDate date, String time) {
        String slot = slotKey(professional, date, time);
        Appointment appointment = booked.get(slot);
        if (appointment == null || !appointment.patient.equals(patient)) {
            return false;
        }
        booked.remove(slot);
        available.computeIfAbsent(agendaKey(professional, date), key -> new LinkedHashSet<>()).add(time);
        return true;
    }

    synchronized List<String> availableSlots(String professional, LocalDate date) {
        Set<String> slots = available.get(agendaKey(professional, date));
        if (slots == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(slots));
    }

    synchronized List<Appointment> appointmentsFor(String professional, LocalDate date) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : booked.values()) {
            if (appointment.professional.equals(professional) && appointment.date.equals(date)) {
                result.add(appointment);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
