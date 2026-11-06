import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Catalog staffScreen = Catalog.getInstance();
        Catalog queryScreen = Catalog.getInstance();

        staffScreen.add("L-101", "Cien años de soledad", "Gabriel García Márquez", 4);
        staffScreen.add("L-102", "El Aleph", "Jorge Luis Borges", 2);
        System.out.println("Búsqueda por título: " + queryScreen.findByTitle("cien años").get(0));
        System.out.println("Búsqueda por autor: " + queryScreen.findByAuthor("Borges").get(0));
        queryScreen.updateCopies("L-101", 3);
        System.out.println("Ejemplares actualizados desde consulta: " + staffScreen.findByCode("L-101").getCopies());
        System.out.println("Mismo catálogo: " + (staffScreen == queryScreen));

        expectInvalid(() -> staffScreen.add("L-101", "Duplicado", "Autor", 1));
        expectInvalid(() -> staffScreen.add("L-103", " ", "Autor", 1));
        expectInvalid(() -> staffScreen.add("L-103", "Libro", "Autor", -1));
        System.out.println("Código duplicado, título vacío y cantidad negativa rechazados: sí.");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Se esperaba rechazar un dato inválido.");
        } catch (IllegalArgumentException expected) {
            // Expected validation result.
        }
    }
}

final class Book {
    private final String code;
    private final String title;
    private final String author;
    private final int copies;

    Book(String code, String title, String author, int copies) {
        this.code = code;
        this.title = title;
        this.author = author;
        this.copies = copies;
    }

    String getCode() {
        return code;
    }

    String getTitle() {
        return title;
    }

    String getAuthor() {
        return author;
    }

    int getCopies() {
        return copies;
    }

    @Override
    public String toString() {
        return code + " - " + title + " / " + author + " (" + copies + " ejemplares)";
    }
}

final class Catalog {
    private final Map<String, Book> books = new LinkedHashMap<>();

    private Catalog() {
    }

    private static class Holder {
        private static final Catalog INSTANCE = new Catalog();
    }

    static Catalog getInstance() {
        return Holder.INSTANCE;
    }

    synchronized void add(String code, String title, String author, int copies) {
        if (code == null || code.trim().isEmpty() || title == null || title.trim().isEmpty()
                || author == null || author.trim().isEmpty() || copies < 0) {
            throw new IllegalArgumentException("Código, título y autor son obligatorios; cantidad no negativa.");
        }
        if (books.containsKey(code.trim())) {
            throw new IllegalArgumentException("El código ya existe: " + code);
        }
        books.put(code.trim(), new Book(code.trim(), title.trim(), author.trim(), copies));
    }

    synchronized Book findByCode(String code) {
        Book book = books.get(code);
        if (book == null) {
            throw new IllegalArgumentException("No existe el código: " + code);
        }
        return book;
    }

    synchronized List<Book> findByTitle(String title) {
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    synchronized List<Book> findByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    synchronized void updateCopies(String code, int copies) {
        if (copies < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        Book current = findByCode(code);
        books.put(code, new Book(current.getCode(), current.getTitle(), current.getAuthor(), copies));
    }
}
