package UI;

import javax.swing.*;
import java.awt.*;

public class Chat extends JFrame {
    private JPanel JPMain;
    private JScrollPane JSPChats;
    private JTextField JTFMessgeInput;
    private JButton JBsend;
    private JPanel JPChatContainer;

    public Chat() {
        this.start();
        this.startListeners();
    }

    private void startListeners() {
        JBsend.addActionListener(e -> {
            System.out.println("Mensaje");
            MensajePanel mensajePanel= new MensajePanel("John Doe","Lorem ipsum");
            JPChatContainer.add(mensajePanel);

            JPChatContainer.revalidate();
            JPChatContainer.repaint();

            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = JSPChats.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }

    private void start() {
        this.setTitle("Chat");
        this.setSize(1200,700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(JPMain);
        JPChatContainer.setLayout(new BoxLayout(JPChatContainer, BoxLayout.Y_AXIS));

    }
}
