import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        SupplyLedger depot = SupplyLedger.getInstance();
        SupplyLedger office = SupplyLedger.getInstance();
        SupplyLedger lab = SupplyLedger.getInstance();

        depot.record("INS-01", MovementType.ENTRY, 20, "Depósito");
        office.record("INS-01", MovementType.EXIT, 4, "Consultorios");
        lab.record("INS-02", MovementType.ENTRY, 8, "Laboratorio");
        lab.record("INS-01", MovementType.EXIT, 3, "Laboratorio");

        System.out.println("Movimientos totales: " + depot.history().size());
        System.out.println("Movimientos de INS-01: " + office.bySupply("INS-01").size());
        System.out.println("Movimientos del Laboratorio: " + lab.bySector("Laboratorio").size());
        System.out.println("Saldo INS-01: " + depot.balance("INS-01"));
        System.out.println("Saldo INS-02: " + depot.balance("INS-02"));
        System.out.println("Mismo registro en los tres sectores: " + (depot == office && office == lab));
        try {
            office.record("INS-01", MovementType.EXIT, 50, "Consultorios");
            throw new AssertionError("La salida superior al saldo debe rechazarse.");
        } catch (IllegalArgumentException expected) {
            System.out.println("Salida que excede saldo rechazada; saldo: " + depot.balance("INS-01"));
        }
        try {
            depot.history().clear();
            throw new AssertionError("El historial no debe ser modificable.");
        } catch (UnsupportedOperationException expected) {
            System.out.println("Historial protegido contra edición y eliminación: sí.");
        }
    }
}

enum MovementType {
    ENTRY, EXIT
}

final class SupplyMovement {
    final String supplyCode;
    final MovementType type;
    final int quantity;
    final String sector;
    final LocalDate date;

    SupplyMovement(String supplyCode, MovementType type, int quantity, String sector) {
        this.supplyCode = supplyCode;
        this.type = type;
        this.quantity = quantity;
        this.sector = sector;
        this.date = LocalDate.now();
    }

    @Override
    public String toString() {
        return date + " | " + supplyCode + " | " + type + " " + quantity + " | " + sector;
    }
}

final class SupplyLedger {
    private final List<SupplyMovement> movements = new ArrayList<>();

    private SupplyLedger() {
    }

    private static class Holder {
        private static final SupplyLedger INSTANCE = new SupplyLedger();
    }

    static SupplyLedger getInstance() {
        return Holder.INSTANCE;
    }

    synchronized void record(String code, MovementType type, int quantity, String sector) {
        if (code == null || code.trim().isEmpty() || type == null || quantity <= 0
                || sector == null || sector.trim().isEmpty()) {
            throw new IllegalArgumentException("Código, tipo, sector y cantidad positiva son obligatorios.");
        }
        if (type == MovementType.EXIT && quantity > balance(code)) {
            throw new IllegalArgumentException("La salida excede el saldo de " + code);
        }
        movements.add(new SupplyMovement(code.trim(), type, quantity, sector.trim()));
    }

    synchronized List<SupplyMovement> history() {
        return Collections.unmodifiableList(new ArrayList<>(movements));
    }

    synchronized List<SupplyMovement> bySupply(String code) {
        return movements.stream().filter(m -> m.supplyCode.equals(code)).collect(Collectors.toList());
    }

    synchronized List<SupplyMovement> bySector(String sector) {
        return movements.stream().filter(m -> m.sector.equalsIgnoreCase(sector)).collect(Collectors.toList());
    }

    synchronized int balance(String code) {
        int balance = 0;
        for (SupplyMovement movement : movements) {
            if (movement.supplyCode.equals(code)) {
                balance += movement.type == MovementType.ENTRY ? movement.quantity : -movement.quantity;
            }
        }
        return balance;
    }
}
