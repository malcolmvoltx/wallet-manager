/*kitandwe shuraim 2600700305
kusasira brighton 2600703541
lubega samuel 2600700315
okiror malcolm emmanuel 2600703629
kobusinge doreen 2600703539itandwe shuraim 2600700305
kusasira brighton 2600703541
lubega samuel 2600700315
okiror malcolm emmanuel 2600703629
kobusinge doreen 2600703539 */
public class Main {
    public static void main(String[] args) {

        WalletManager manager = new WalletManager();

        // Demo wallets so the app is not empty on first launch.
        Wallet amina = manager.addWallet("Amina", "1234");
        amina.deposit(10000);
        Wallet brian = manager.addWallet("Brian", "4321");
        brian.deposit(5000);

        // Swing windows should be created on the Event Dispatch Thread.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WalletApp(manager).setVisible(true);
            }
        });
    }
}
