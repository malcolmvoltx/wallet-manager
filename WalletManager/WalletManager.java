/**
 * WalletManager - keeps all wallets in an ARRAY and does the operations
 * that involve more than one wallet (create, find, transfer, totals).
 */
public class WalletManager {

    public static final int MAX_WALLETS = 20;

    private Wallet[] wallets = new Wallet[MAX_WALLETS];   // array of objects
    private int count = 0;
    private int nextNumber = 1;
    private String lastMessage = "";

    public int getCount()          { return count; }
    public String getLastMessage() { return lastMessage; }

    public Wallet getWallet(int index) {
        if (index < 0 || index >= count) {
            return null;
        }
        return wallets[index];
    }

    // ---------- CREATE ----------
    public Wallet addWallet(String owner, String pin) {
        if (count >= MAX_WALLETS) {
            lastMessage = "Wallet list is full (" + MAX_WALLETS + ").";
            return null;
        }
        if (owner == null || owner.trim().isEmpty()) {
            lastMessage = "Owner name cannot be empty.";
            return null;
        }
        Wallet w = new Wallet(owner.trim(), generateId());
        if (!w.setPin(pin)) {                    // PIN rules live inside Wallet
            lastMessage = w.getLastMessage();
            return null;
        }
        wallets[count] = w;
        count++;
        lastMessage = "Created wallet " + w.getWalletId() + " for " + w.getOwner() + ".";
        return w;
    }

    // do-while: keep making IDs until one is not already used.
    private String generateId() {
        String id;
        do {
            id = String.format("W-%03d", nextNumber);
            nextNumber++;
        } while (find(id) != null);
        return id;
    }

    // ---------- FIND ----------
    // while loop: walk through the array until we find the ID.
    public Wallet find(String id) {
        int i = 0;
        while (i < count) {
            if (wallets[i].getWalletId().equalsIgnoreCase(id)) {
                return wallets[i];
            }
            i++;
        }
        return null;
    }

    // ---------- TRANSFER ----------
    public boolean transfer(Wallet from, String toId, double amount, String pin) {
        Wallet to = find(toId);
        if (to == null) {
            lastMessage = "No wallet with ID " + toId + ".";
            return false;
        }
        if (to == from) {
            lastMessage = "Cannot transfer to the same wallet.";
            return false;
        }
        if (!from.withdraw(amount, pin, "TRANSFER OUT")) {
            lastMessage = from.getLastMessage();
            return false;
        }
        to.deposit(amount, "TRANSFER IN");
        lastMessage = String.format("Sent %,.2f from %s to %s.",
                amount, from.getWalletId(), to.getWalletId());
        return true;
    }

    // ---------- TOTALS ----------
    // for loop: add up every balance.
    public double getTotalBalance() {
        double total = 0;
        for (int i = 0; i < count; i++) {
            total += wallets[i].getBalance();
        }
        return total;
    }

    public int getLockedCount() {
        int n = 0;
        for (int i = 0; i < count; i++) {
            if (wallets[i].isLocked()) {
                n++;
            }
        }
        return n;
    }
}
