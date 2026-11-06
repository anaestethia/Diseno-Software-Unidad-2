public interface PaymentGateway {
    String charge(String account, long amountInCents);
}
