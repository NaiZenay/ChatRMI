package UI;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import client.Client;
import client.RemoteChat;
import server.ServerChatInterface;

public class Chat extends JFrame implements RemoteChat {
    private JPanel JPMain;
    private JScrollPane JSPChats;
    private JTextField JTFMessgeInput;
    private JButton JBsend;
    private JPanel JPChatContainer;
    private Client client;
    private ServerChatInterface servidor;

    public Chat(Client client) throws RemoteException {
        this.client = client;
        UnicastRemoteObject.exportObject(this, 0); // Cambié a 0 para puerto automático
        this.start();
        this.startListeners();
        this.conectarAlServidor();
    }

    private void conectarAlServidor() {
        String ipServidor = JOptionPane.showInputDialog(
                this,
                "Ingresa la IP del servidor:",
                "Conectar al Chat",
                JOptionPane.QUESTION_MESSAGE
        );

        if (ipServidor == null || ipServidor.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes ingresar una IP");
            System.exit(0);
            return;
        }

        try {
            Registry registry = LocateRegistry.getRegistry(ipServidor.trim(), 1099);
            servidor = (ServerChatInterface) registry.lookup("ChatServidor");
            servidor.registrarCliente(this);

            JOptionPane.showMessageDialog(this,
                    "Conectado exitosamente al servidor",
                    "Conexión Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar al servidor en " + ipServidor + "\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    @Override
    public void getMessage(String emisor, String mensaje) throws RemoteException {
        // CRÍTICO: Actualizar UI en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            agregarMensajeAlChat(emisor, mensaje);
        });
    }

    @Override
    public String getNombre() throws RemoteException {
        return client.getName();
    }

    private void agregarMensajeAlChat(String emisor, String mensaje) {
        MensajePanel mensajePanel = new MensajePanel(emisor, mensaje);
        JPChatContainer.add(mensajePanel);
        JPChatContainer.add(Box.createVerticalStrut(10));

        JPChatContainer.revalidate();
        JPChatContainer.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = JSPChats.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void startListeners() {
        JBsend.addActionListener(e -> enviarMensaje());
        JTFMessgeInput.addActionListener(e -> enviarMensaje());
    }

    private void enviarMensaje() {
        String mensaje = JTFMessgeInput.getText().trim();

        if (mensaje.isEmpty()) return;

        try {
            // Enviar mensaje al servidor
            servidor.enviarMensaje(client.getName(), mensaje);
            JTFMessgeInput.setText("");

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Error enviando mensaje: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void start() {
        this.setTitle("Chat - " + client.getName());
        this.setSize(1200, 700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(JPMain);
        JPChatContainer.setLayout(new BoxLayout(JPChatContainer, BoxLayout.Y_AXIS));

        // Desregistrarse al cerrar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    if (servidor != null) {
                        servidor.desregistrarCliente(Chat.this);
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}