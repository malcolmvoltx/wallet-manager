public class Wallet {

    private static final int MAX_HISTORY = 100;


    private String owner;
    private final String walletId;
    private double balance;
    private String pin;
    private int wrongPinAttempts = 0;
    private boolean locked = false;

    private Transaction[] history = new Transaction[MAX_HISTORY];  // array of objects
    private int historyCount = 0;

    private String lastMessage = "";

   
    public Wallet(String owner, String walletId) {
        this.owner = owner;
        this.walletId = walletId;
        this.balance = 0;
    }

   
    public String getOwner()       { return owner; }
    public void setOwner(String owner) {
        if (owner != null && !owner.trim().isEmpty()) {
            this.owner = owner.trim();
        }
    }
    public String getWalletId()    { return walletId; }      // read-only
    public double getBalance()     { return balance; }
    public boolean isLocked()      { return locked; }
    public String getLastMessage() { return lastMessage; }
    public int getHistoryCount()   { return historyCount; }

    public Transaction getTransaction(int index) {
        if (index < 0 || index >= historyCount) {
            return null;
        }
        return history[index];
    }
   

    
    public boolean setPin(String newPin) {
        if (!isValidPin(newPin)) {
            lastMessage = "PIN must be exactly 4 digits.";
            return false;
        }
        this.pin = newPin;
        lastMessage = "PIN saved.";
        return true;
    }

    public boolean deposit(double amount) {
        return deposit(amount, "DEPOSIT");
    }

   
    public boolean deposit(double amount, String type) {
        if (amount <= 0) {
            lastMessage = "Deposit must be more than 0.";
            return false;
        }
        balance += amount;
        record(type, amount);
        lastMessage = String.format("Deposited %,.2f.", amount);
        return true;
    }

   
    public boolean withdraw(double amount, String enteredPin) {
        return withdraw(amount, enteredPin, "WITHDRAW");
    }

    public boolean withdraw(double amount, String enteredPin, String type) {
        // 1. locked?
        if (locked) {
            lastMessage = "Wallet is LOCKED. Reset the PIN to unlock it.";
            return false;
        }
        // 2. PIN correct?
        if (pin == null || !pin.equals(enteredPin)) {
            wrongPinAttempts++;
            if (wrongPinAttempts >= 3) {
                locked = true;
                lastMessage = "Wrong PIN (3 of 3). Wallet is now LOCKED.";
            } else {
                lastMessage = "Wrong PIN (attempt " + wrongPinAttempts + " of 3).";
            }
            return false;
        }
        // 3. amount OK?
        if (amount <= 0) {
            lastMessage = "Amount must be more than 0.";
            return false;
        }
        if (amount > balance) {
            lastMessage = "Insufficient balance.";
            return false;
        }
        // 4. success
        balance -= amount;
        wrongPinAttempts = 0;
        record(type, amount);
        lastMessage = String.format("Withdrew %,.2f.", amount);
        return true;
    }

    // ---------- RESET (unlock + new PIN) ----------
    public boolean reset(String oldPin, String newPin) {
        if (pin == null || !pin.equals(oldPin)) {
            lastMessage = "Reset failed: old PIN is wrong.";
            return false;
        }
        if (!isValidPin(newPin)) {
            lastMessage = "Reset failed: new PIN must be exactly 4 digits.";
            return false;
        }
        this.pin = newPin;
        this.wrongPinAttempts = 0;
        this.locked = false;
        lastMessage = "PIN changed and wallet unlocked.";
        return true;
    }

    // ---------- PRIVATE HELPERS ----------
    private boolean isValidPin(String p) {
        return p != null && p.matches("\\d{4}");
    }

    // Adds to the history array; when full, the oldest entry is dropped.
    private void record(String type, double amount) {
        if (historyCount == MAX_HISTORY) {
            for (int i = 1; i < MAX_HISTORY; i++) {
                history[i - 1] = history[i];
            }
            historyCount--;
        }
        history[historyCount] = new Transaction(type, amount, balance);
        historyCount++;
    }
}
