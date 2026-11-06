import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ThemeManager {
    INSTANCE;

    public enum Theme {
        LIGHT,
        DARK
    }

    private Theme currentTheme = Theme.LIGHT;
    private final Map<String, String> lightColors;
    private final Map<String, String> darkColors;

    ThemeManager() {
        Map<String, String> light = new HashMap<>();
        light.put("background", "#FFFFFF");
        light.put("text", "#202124");
        light.put("accent", "#1769AA");
        lightColors = Collections.unmodifiableMap(light);

        Map<String, String> dark = new HashMap<>();
        dark.put("background", "#202124");
        dark.put("text", "#F1F3F4");
        dark.put("accent", "#8AB4F8");
        darkColors = Collections.unmodifiableMap(dark);
    }

    public synchronized void setTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("Theme must not be null");
        }
        currentTheme = theme;
    }

    public synchronized Theme getTheme() {
        return currentTheme;
    }

    public synchronized String getColor(String component) {
        if (component == null || component.isBlank()) {
            throw new IllegalArgumentException("Component must not be blank");
        }
        Map<String, String> palette = currentTheme == Theme.LIGHT ? lightColors : darkColors;
        String color = palette.get(component.toLowerCase(Locale.ROOT));
        if (color == null) {
            throw new IllegalArgumentException("Unknown color component: " + component);
        }
        return color;
    }
}
