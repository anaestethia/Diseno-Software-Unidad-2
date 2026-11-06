public final class SingletonPaymentGateway implements PaymentGateway {
    private SingletonPaymentGateway() {
    }

    private static class Holder {
        private static final SingletonPaymentGateway INSTANCE =
                new SingletonPaymentGateway();
    }

    public static SingletonPaymentGateway getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String charge(String account, long amountInCents) {
        validatePayment(account, amountInCents);
        return "Pago aprobado por $" + String.format("%.2f", amountInCents / 100.0)
                + " para " + account;
    }

    private static void validatePayment(String account, long amountInCents) {
        if (account == null || account.isBlank() || amountInCents <= 0) {
            throw new IllegalArgumentException("Account and positive amount are required");
        }
    }
}
