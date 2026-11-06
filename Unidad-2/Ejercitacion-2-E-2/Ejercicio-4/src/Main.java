import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        LoanManager counterA = LoanManager.getInstance();
        LoanManager counterB = LoanManager.getInstance();
        counterA.loan("COPY-001", "SOCIO-10", LocalDate.of(2026, 11, 6));

        boolean secondLoanAccepted = counterB.loan("COPY-001", "SOCIO-20", LocalDate.now());
        System.out.println("Segundo mostrador prestó ejemplar ocupado: " + secondLoanAccepted);
        System.out.println("Préstamos activos de SOCIO-10: " + counterB.activeFor("SOCIO-10").size());
        System.out.println("Mismo administrador: " + (counterA == counterB));
        System.out.println("Devolución sin préstamo activo: " + counterB.returnCopy("COPY-999"));
        System.out.println("El préstamo original continúa activo: " + counterA.activeFor("SOCIO-10").size());
        System.out.println("Devolución válida: " + counterB.returnCopy("COPY-001"));
        System.out.println("Se puede volver a prestar: "
                + counterA.loan("COPY-001", "SOCIO-20", LocalDate.now()));
    }
}

final class Loan {
    final String copyId;
    final String memberId;
    final LocalDate date;

    Loan(String copyId, String memberId, LocalDate date) {
        this.copyId = copyId;
        this.memberId = memberId;
        this.date = date;
    }
}

final class LoanManager {
    private final Map<String, Loan> activeLoans = new LinkedHashMap<>();

    private LoanManager() {
    }

    private static class Holder {
        private static final LoanManager INSTANCE = new LoanManager();
    }

    static LoanManager getInstance() {
        return Holder.INSTANCE;
    }

    boolean loan(String copyId, String memberId, LocalDate date) {
        if (copyId == null || copyId.trim().isEmpty() || memberId == null || memberId.trim().isEmpty()
                || date == null) {
            throw new IllegalArgumentException("Ejemplar, socio y fecha son obligatorios.");
        }
        if (activeLoans.containsKey(copyId)) {
            return false;
        }
        activeLoans.put(copyId, new Loan(copyId, memberId, date));
        return true;
    }

    boolean returnCopy(String copyId) {
        if (!activeLoans.containsKey(copyId)) {
            return false;
        }
        activeLoans.remove(copyId);
        return true;
    }

    List<Loan> activeFor(String memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : activeLoans.values()) {
            if (loan.memberId.equals(memberId)) {
                result.add(loan);
            }
        }
        return result;
    }
}
