import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ProfileCache {
    private static final int CAPACITY = 3;

    private final Map<String, String> profiles =
            new LinkedHashMap<String, String>(CAPACITY, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > CAPACITY;
                }
            };

    private ProfileCache() {
    }

    private static class Holder {
        private static final ProfileCache INSTANCE = new ProfileCache();
    }

    public static ProfileCache getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void put(String key, String profile) {
        validateKey(key);
        if (profile == null) {
            throw new IllegalArgumentException("Profile must not be null");
        }
        profiles.put(key, profile);
    }

    public synchronized Optional<String> get(String key) {
        validateKey(key);
        return Optional.ofNullable(profiles.get(key));
    }

    public synchronized boolean contains(String key) {
        validateKey(key);
        return profiles.containsKey(key);
    }

    public synchronized boolean remove(String key) {
        validateKey(key);
        return profiles.remove(key) != null;
    }

    public synchronized int size() {
        return profiles.size();
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key must not be blank");
        }
    }
}
