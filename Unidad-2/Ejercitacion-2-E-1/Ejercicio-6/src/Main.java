public class Main {
    public static void main(String[] args) {
        ThemeManager manager = ThemeManager.INSTANCE;
        ThemeManager viewA = ThemeManager.INSTANCE;
        ThemeManager viewB = ThemeManager.INSTANCE;

        System.out.println("Vistas comparten Singleton: "
                + (manager == viewA && viewA == viewB));
        System.out.println("Tema inicial: " + viewA.getTheme()
                + "; fondo=" + viewA.getColor("background")
                + "; texto=" + viewB.getColor("text"));

        viewB.setTheme(ThemeManager.Theme.DARK);
        System.out.println("Cambio desde vista B: " + manager.getTheme());
        System.out.println("Vista A ve fondo=" + viewA.getColor("background")
                + "; acento=" + viewA.getColor("accent"));
    }
}
