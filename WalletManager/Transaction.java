import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class Transaction {

    private final String type;          // e.g. "DEPOSIT", "WITHDRAW"
    private final double amount;
    private final double balanceAfter;
    private final String time;

    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String getType()         { return type; }
    public double getAmount()       { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getTime()         { return time; }

    @Override
    public String toString() {
        return String.format("%s  %-13s %,12.2f   balance: %,.2f",
                time, type, amount, balanceAfter);
    }
}
