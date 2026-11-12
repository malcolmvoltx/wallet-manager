import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class WalletApp extends JFrame implements ActionListener {

    private WalletManager manager;

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private JLabel summaryLabel;

    public WalletApp(WalletManager manager) {
        super("Wallet Manager");
        this.manager = manager;
        buildWindow();
        refresh();
    }

    //  BUILD THE WINDOW 
    private void buildWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(760, 460);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // top: title + summary
        JPanel top = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("  Wallet Manager", JLabel.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        summaryLabel = new JLabel("  ");
        top.add(title);
        top.add(summaryLabel);
        add(top, BorderLayout.NORTH);

        // centre: table of wallets
        String[] columns = {"ID", "Owner", "Balance", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // right: buttons
        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
        String[] names = {"New Wallet", "Deposit", "Withdraw", "Transfer", "Reset PIN", "History"};
        for (int i = 0; i < names.length; i++) {
            JButton b = new JButton(names[i]);
            b.setActionCommand(names[i]);
            b.addActionListener(this);
            buttons.add(b);
        }
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        right.add(buttons, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        // bottom: status line
        statusLabel = new JLabel("  Select a wallet, then choose an action.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
        add(statusLabel, BorderLayout.SOUTH);
    }

    //  BUTTON CLICKS (switch) 
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "New Wallet":
                newWallet();
                break;
            case "Deposit":
                deposit();
                break;
            case "Withdraw":
                withdraw();
                break;
            case "Transfer":
                transfer();
                break;
            case "Reset PIN":
                resetPin();
                break;
            case "History":
                showHistory();
                break;
            default:
                setStatus("Unknown action: " + command);
        }
    }

    //  ACTIONS 
    private void newWallet() {
        JTextField ownerField = new JTextField();
        JPasswordField pinField = new JPasswordField();
        Object[] form = {"Owner name:", ownerField, "Choose a 4-digit PIN:", pinField};

        int answer = JOptionPane.showConfirmDialog(this, form, "New Wallet",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        manager.addWallet(ownerField.getText(), new String(pinField.getPassword()));
        setStatus(manager.getLastMessage());
        refresh();
    }

    private void deposit() {
        Wallet w = selectedWallet();
        if (w == null) return;

        double amount = askAmount("Amount to deposit into " + w.getWalletId() + ":");
        if (Double.isNaN(amount)) return;

        w.deposit(amount);
        setStatus(w.getLastMessage());
        refresh();
    }

    private void withdraw() {
        Wallet w = selectedWallet();
        if (w == null) return;

        double amount = askAmount("Amount to withdraw from " + w.getWalletId() + ":");
        if (Double.isNaN(amount)) return;
        String pin = askPin("Enter PIN for " + w.getOwner() + ":");
        if (pin == null) return;

        w.withdraw(amount, pin);
        setStatus(w.getLastMessage());
        refresh();
    }

    private void transfer() {
        Wallet w = selectedWallet();
        if (w == null) return;

        String toId = JOptionPane.showInputDialog(this,"Send from " + w.getWalletId() + " to wallet ID (e.g. W-002):");
        if (toId == null) return;
        double amount = askAmount("Amount to send:");
        if (Double.isNaN(amount)) return;
        String pin = askPin("Enter YOUR PIN (" + w.getOwner() + "):");
        if (pin == null) return;

        manager.transfer(w, toId.trim(), amount, pin);
        setStatus(manager.getLastMessage());
        refresh();
    }

    private void resetPin() {
        Wallet w = selectedWallet();
        if (w == null) return;

        JPasswordField oldPin = new JPasswordField();
        JPasswordField newPin = new JPasswordField();
        Object[] form = {"Old PIN:", oldPin, "New 4-digit PIN:", newPin};

        int answer = JOptionPane.showConfirmDialog(this, form, "Reset PIN - " + w.getWalletId(),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        w.reset(new String(oldPin.getPassword()), new String(newPin.getPassword()));
        setStatus(w.getLastMessage());
        refresh();
    }

    private void showHistory() {
        Wallet w = selectedWallet();
        if (w == null) return;

        StringBuilder text = new StringBuilder();
        if (w.getHistoryCount() == 0) {
            text.append("No transactions yet.");
        }
        for (int i = 0; i < w.getHistoryCount(); i++) {       // for loop over the array
            text.append(w.getTransaction(i)).append("\n");
        }
        JTextArea area = new JTextArea(text.toString(), 12, 62);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "History - " + w.getOwner() + " (" + w.getWalletId() + ")",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ---------- HELPERS ----------
    // Returns the selected wallet, or null (with a message) if none is chosen.
    private Wallet selectedWallet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            setStatus("Please select a wallet in the table first.");
            return null;
        }
        return manager.getWallet(row);
    }

    // while loop: keep asking until a valid number or Cancel.
    // Returns NaN when the user cancels.
    private double askAmount(String message) {
        while (true) {
            String text = JOptionPane.showInputDialog(this, message);
            if (text == null) {
                return Double.NaN;
            }
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please type a number, e.g. 2500",
                        "Invalid amount", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Returns the typed PIN, or null if the user cancels.
    private String askPin(String message) {
        JPasswordField field = new JPasswordField();
        Object[] form = {message, field};
        int answer = JOptionPane.showConfirmDialog(this, form, "PIN",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(field.getPassword());
    }

    private void setStatus(String message) {
        statusLabel.setText("  " + message);
    }

    // Rebuild the table from the array of wallets.
    private void refresh() {
        tableModel.setRowCount(0);
        for (int i = 0; i < manager.getCount(); i++) {
            Wallet w = manager.getWallet(i);
            String status;
            if (w.isLocked()) {
                status = "LOCKED";
            } else {
                status = "Active";
            }
            tableModel.addRow(new Object[]{
                    w.getWalletId(), w.getOwner(),
                    String.format("%,.2f", w.getBalance()), status});
        }
        summaryLabel.setText(String.format("  Wallets: %d    Total balance: %,.2f    Locked: %d",
                manager.getCount(), manager.getTotalBalance(), manager.getLockedCount()));
    }
}
