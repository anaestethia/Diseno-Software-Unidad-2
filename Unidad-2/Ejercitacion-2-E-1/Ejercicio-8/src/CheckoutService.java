public final class CheckoutService {
    private final PaymentGateway paymentGateway;

    public CheckoutService(PaymentGateway paymentGateway) {
        if (paymentGateway == null) {
            throw new IllegalArgumentException("Payment gateway must not be null");
        }
        this.paymentGateway = paymentGateway;
    }

    public String checkout(String account, long amountInCents) {
        return paymentGateway.charge(account, amountInCents);
    }
}
