import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        int constructionsBeforeFirstAccess = HospitalConfiguration.constructionCount();
        HospitalConfiguration reception = HospitalConfiguration.getInstance();
        reception.setHospitalName("Hospital del Parque");
        reception.setOpeningHour(8);
        reception.setClosingHour(18);
        reception.setAppointmentMinutes(20);

        HospitalConfiguration offices = HospitalConfiguration.getInstance();
        System.out.println("Recepción: " + reception.summary());
        System.out.println("Consultorios: " + offices.summary());
        System.out.println("Construcciones antes del primer acceso: " + constructionsBeforeFirstAccess);
        System.out.println("Instancias creadas después de dos accesos: "
                + HospitalConfiguration.constructionCount());
        System.out.println("Misma instancia: " + (reception == offices));
        try {
            offices.setClosingHour(7);
            throw new AssertionError("El cierre antes de la apertura debe rechazarse.");
        } catch (IllegalArgumentException expected) {
            System.out.println("Cierre anterior a apertura rechazado: sí.");
        }
    }
}

final class HospitalConfiguration {
    private static final AtomicInteger CONSTRUCTIONS = new AtomicInteger();
    private String hospitalName = "Hospital";
    private int openingHour = 8;
    private int closingHour = 17;
    private int appointmentMinutes = 30;

    private HospitalConfiguration() {
        CONSTRUCTIONS.incrementAndGet();
    }

    private static class Holder {
        private static final HospitalConfiguration INSTANCE = new HospitalConfiguration();
    }

    static HospitalConfiguration getInstance() {
        return Holder.INSTANCE;
    }

    static int constructionCount() {
        return CONSTRUCTIONS.get();
    }

    String summary() {
        return hospitalName + " | " + openingHour + ":00-" + closingHour + ":00 | turnos de "
                + appointmentMinutes + " min";
    }

    void setHospitalName(String hospitalName) {
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.hospitalName = hospitalName.trim();
    }

    void setOpeningHour(int openingHour) {
        if (openingHour < 0 || openingHour > 23 || closingHour <= openingHour) {
            throw new IllegalArgumentException("Horario de apertura no válido.");
        }
        this.openingHour = openingHour;
    }

    void setClosingHour(int closingHour) {
        if (closingHour < 0 || closingHour > 23 || closingHour <= openingHour) {
            throw new IllegalArgumentException("El cierre debe ser posterior a la apertura.");
        }
        this.closingHour = closingHour;
    }

    void setAppointmentMinutes(int appointmentMinutes) {
        if (appointmentMinutes <= 0) {
            throw new IllegalArgumentException("La duración debe ser positiva.");
        }
        this.appointmentMinutes = appointmentMinutes;
    }
}
