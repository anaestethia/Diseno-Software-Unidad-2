public class Main {
    public static void main(String[] args) {
        ProfileCache cache = ProfileCache.getInstance();
        System.out.println("Singleton compartido: " + (cache == ProfileCache.getInstance()));

        cache.put("u-01", "Ana");
        cache.put("u-02", "Luis");
        cache.put("u-03", "Marta");
        cache.get("u-01");
        cache.put("u-04", "Noah");

        System.out.println("u-01 conservado tras LRU: " + cache.get("u-01").orElse("ausente"));
        System.out.println("u-02 expulsado por capacidad: " + !cache.contains("u-02"));
        cache.put("u-03", "Marta actualizada");
        System.out.println("Actualizacion: " + cache.get("u-03").orElse("ausente"));
        System.out.println("Eliminacion u-04: " + cache.remove("u-04"));
        System.out.println("Entradas actuales: " + cache.size() + " (maximo 3)");
    }
}
