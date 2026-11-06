public class Main {
    public static void main(String[] args) {
        SingletonPaymentGateway gateway = SingletonPaymentGateway.getInstance();
        CheckoutService productionCheckout = new CheckoutService(gateway);
        System.out.println("Singleton compartido: "
                + (gateway == SingletonPaymentGateway.getInstance()));
        System.out.println("Produccion: "
                + productionCheckout.checkout("cuenta-17", 2599));

        RecordingFakeGateway fake = new RecordingFakeGateway();
        CheckoutService testCheckout = new CheckoutService(fake);
        System.out.println("Prueba con doble: " + testCheckout.checkout("test-user", 500));
        System.out.println("Llamadas registradas por el doble: " + fake.calls);
    }

    private static final class RecordingFakeGateway implements PaymentGateway {
        private int calls;

        @Override
        public String charge(String account, long amountInCents) {
            calls++;
            return "Simulacion aceptada para " + account
                    + " (" + amountInCents + " centavos)";
        }
    }
}
