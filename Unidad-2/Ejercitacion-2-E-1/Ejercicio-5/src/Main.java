import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        HardwareController controller = HardwareController.INSTANCE;
        System.out.println("Singleton compartido: "
                + (controller == HardwareController.INSTANCE));
        System.out.println("Disponible antes de empezar: " + controller.isAvailable());

        CountDownLatch start = new CountDownLatch(1);
        List<Thread> operators = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            final int commandId = i;
            Thread operator = new Thread(() -> {
                try {
                    start.await();
                    controller.sendCommand("mover-brazo-" + commandId);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }, "Operador-" + i);
            operators.add(operator);
            operator.start();
        }

        start.countDown();
        for (Thread operator : operators) {
            operator.join();
        }
        System.out.println("Estado final: " + controller.getStatus());
        System.out.println("Disponible al finalizar: " + controller.isAvailable());
    }
}
