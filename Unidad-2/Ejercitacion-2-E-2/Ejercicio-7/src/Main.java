import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TicketNumberGenerator generator = TicketNumberGenerator.getInstance();
        String[] terminals = {"Terminal-A", "Terminal-B", "Terminal-C"};
        for (int request = 0; request < 21; request++) {
            generator.issue(terminals[request % terminals.length]);
        }
        System.out.println("Números emitidos: " + generator.history().size());
        System.out.println("Último número (sin incrementar): " + generator.lastNumber());
        System.out.println("Primeros tres: " + generator.history().subList(0, 3));
        System.out.println("Últimos tres: " + generator.history().subList(18, 21));
        System.out.println("Contador consecutivo y sin repetidos: "
                + (generator.lastNumber() == 21 && generator.history().get(0).number == 1));
    }
}

final class Ticket {
    final int number;
    final String terminal;

    Ticket(int number, String terminal) {
        this.number = number;
        this.terminal = terminal;
    }

    @Override
    public String toString() {
        return number + "@" + terminal;
    }
}

final class TicketNumberGenerator {
    private int lastNumber;
    private final List<Ticket> history = new ArrayList<>();

    private TicketNumberGenerator() {
    }

    private static class Holder {
        private static final TicketNumberGenerator INSTANCE = new TicketNumberGenerator();
    }

    static TicketNumberGenerator getInstance() {
        return Holder.INSTANCE;
    }

    Ticket issue(String terminal) {
        if (terminal == null || terminal.trim().isEmpty()) {
            throw new IllegalArgumentException("La terminal es obligatoria.");
        }
        Ticket ticket = new Ticket(++lastNumber, terminal);
        history.add(ticket);
        return ticket;
    }

    int lastNumber() {
        return lastNumber;
    }

    List<Ticket> history() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }
}
