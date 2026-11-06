public enum HardwareController {
    INSTANCE;

    public enum Status {
        AVAILABLE,
        BUSY
    }

    private Status status = Status.AVAILABLE;

    public synchronized boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized void sendCommand(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Command must not be blank");
        }
        if (!isAvailable()) {
            System.out.println("Dispositivo ocupado; comando rechazado: " + command);
            return;
        }

        status = Status.BUSY;
        try {
            System.out.println(Thread.currentThread().getName()
                    + " -> ejecutando: " + command + " (estado: " + status + ")");
            try {
                Thread.sleep(25);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                System.out.println("Comando interrumpido: " + command);
            }
        } finally {
            status = Status.AVAILABLE;
        }
    }
}
