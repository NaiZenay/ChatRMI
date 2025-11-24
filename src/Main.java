import UI.Chat;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Chat chat = new Chat();
        SwingUtilities.invokeLater(()-> chat.setVisible(true));
    }
}
