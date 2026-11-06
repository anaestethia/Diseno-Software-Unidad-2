public class Main {
    public static void main(String[] args) {
        LibraryConfiguration loans = LibraryConfiguration.getInstance();
        LibraryConfiguration queries = LibraryConfiguration.getInstance();

        loans.setName("Biblioteca Central");
        loans.setMaxBooksPerMember(5);
        loans.setReturnDays(14);

        System.out.println("Préstamos: " + loans.getName() + ", máximo " + loans.getMaxBooksPerMember()
                + " libros, devolución en " + loans.getReturnDays() + " días.");
        System.out.println("Consultas: " + queries.getName() + ", máximo " + queries.getMaxBooksPerMember()
                + " libros, devolución en " + queries.getReturnDays() + " días.");
        System.out.println("Misma instancia: " + (loans == queries));

        expectInvalid(() -> loans.setName(" "));
        expectInvalid(() -> loans.setMaxBooksPerMember(0));
        expectInvalid(() -> loans.setReturnDays(-1));
        System.out.println("Validaciones de nombre y valores positivos: correctas.");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Se esperaba rechazar un valor inválido.");
        } catch (IllegalArgumentException expected) {
            // Expected validation result.
        }
    }
}

final class LibraryConfiguration {
    private String name = "Biblioteca";
    private int maxBooksPerMember = 3;
    private int returnDays = 21;

    private LibraryConfiguration() {
    }

    private static class Holder {
        private static final LibraryConfiguration INSTANCE = new LibraryConfiguration();
    }

    static LibraryConfiguration getInstance() {
        return Holder.INSTANCE;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.name = name.trim();
    }

    int getMaxBooksPerMember() {
        return maxBooksPerMember;
    }

    void setMaxBooksPerMember(int maxBooksPerMember) {
        if (maxBooksPerMember <= 0) {
            throw new IllegalArgumentException("El máximo debe ser positivo.");
        }
        this.maxBooksPerMember = maxBooksPerMember;
    }

    int getReturnDays() {
        return returnDays;
    }

    void setReturnDays(int returnDays) {
        if (returnDays <= 0) {
            throw new IllegalArgumentException("El plazo debe ser positivo.");
        }
        this.returnDays = returnDays;
    }
}
