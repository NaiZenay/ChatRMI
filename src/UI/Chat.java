package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import client.Client;
import client.RemoteChat;
import server.ServerChatInterface;

public class Chat extends JFrame implements RemoteChat {
    private JPanel JPMain;
    private JScrollPane JSPChats;
    private JTextField JTFMessgeInput;
    private JButton JBsend;
    private JButton JBChatDirecto;
    private JPanel JPChatContainer;
    private Client client;
    private ServerChatInterface servidor;

    public Chat(Client client) throws RemoteException {
        this.client = client;

        // Crear registry local para este cliente
        client.crearRegistryLocal(this);

        this.initComponents(); // Initialize UI components manually
        this.start();
        this.startListeners();
        this.conectarAlServidor();
    }

    private void initComponents() {
        // Main Panel
        JPMain = new JPanel(new BorderLayout());

        // Chat Container (where messages appear)
        JPChatContainer = new JPanel();
        JPChatContainer.setLayout(new BoxLayout(JPChatContainer, BoxLayout.Y_AXIS));
        JSPChats = new JScrollPane(JPChatContainer);
        JSPChats.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Input Panel (Text field + Send button)
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JTFMessgeInput = new JTextField();
        JBsend = new JButton("Enviar");
        inputPanel.add(JTFMessgeInput, BorderLayout.CENTER);
        inputPanel.add(JBsend, BorderLayout.EAST);

        // Top Panel (Direct Chat button)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JBChatDirecto = new JButton("Chat Directo P2P");
        topPanel.add(JBChatDirecto);

        // Add everything to Main Panel
        JPMain.add(topPanel, BorderLayout.NORTH);
        JPMain.add(JSPChats, BorderLayout.CENTER);
        JPMain.add(inputPanel, BorderLayout.SOUTH);
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
            Registry registry = LocateRegistry.getRegistry(ipServidor.trim(), 1099); // Changed port to 1099 (default RMI) as per ServerChat.java
            servidor = (ServerChatInterface) registry.lookup("ChatServidor");
            servidor.registrarCliente(this);

            JOptionPane.showMessageDialog(this,
                    "Conectado exitosamente al servidor\n" +
                            "Tu IP: " + client.getIp() + "\n" +
                            "Tu Puerto: " + client.getPort(),
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
        SwingUtilities.invokeLater(() -> {
            agregarMensajeAlChat(emisor, mensaje, false);
        });
    }

    @Override
    public void getMensajePrivado(String emisor, String mensaje) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            agregarMensajeAlChat("[PRIVADO] " + emisor, mensaje, true);
        });
    }

    @Override
    public String getNombre() throws RemoteException {
        return client.getName();
    }

    private void iniciarChatDirecto() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField tfIP = new JTextField();
        JTextField tfPuerto = new JTextField();
        JTextField tfNombre = new JTextField();

        panel.add(new JLabel("IP del otro cliente:"));
        panel.add(tfIP);
        panel.add(new JLabel("Puerto del otro cliente:"));
        panel.add(tfPuerto);
        panel.add(new JLabel("Nombre en registry:"));
        panel.add(tfNombre);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Conectar a Cliente Directamente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String ip = tfIP.getText().trim();
            String puertoStr = tfPuerto.getText().trim();
            String nombreRemoto = tfNombre.getText().trim();

            if (ip.isEmpty() || puertoStr.isEmpty() || nombreRemoto.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Todos los campos son obligatorios",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int puerto = Integer.parseInt(puertoStr);
                conectarClienteDirecto(ip, puerto, nombreRemoto);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Puerto inválido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void conectarClienteDirecto(String ip, int puerto, String nombreRemoto) {
        try {
            // Conectar directamente al registry del otro cliente
            Registry registryRemoto = LocateRegistry.getRegistry(ip, puerto);
            RemoteChat clienteRemoto = (RemoteChat) registryRemoto.lookup("ChatCliente_" + nombreRemoto);

            // Crear ventana de chat privado
            abrirVentanaChatPrivado(clienteRemoto, nombreRemoto);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar al cliente:\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirVentanaChatPrivado(RemoteChat clienteRemoto, String nombreDestino) {
        JDialog chatPrivado = new JDialog(this, "Chat Directo con " + nombreDestino, false);
        chatPrivado.setSize(600, 400);
        chatPrivado.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(240, 240, 240)); // Lighter background for better visibility
        JScrollPane scrollPane = new JScrollPane(chatPanel);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Enviar");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        chatPrivado.add(mainPanel);

        Runnable enviarMensajePrivado = () -> {
            String mensaje = inputField.getText().trim();
            if (!mensaje.isEmpty()) {
                try {
                    // Enviar DIRECTAMENTE sin pasar por servidor
                    clienteRemoto.getMensajePrivado(client.getName(), mensaje);

                    // Mostrar en mi chat
                    MensajePanel mensajePanel = new MensajePanel("Tú", mensaje);
                    chatPanel.add(mensajePanel);
                    chatPanel.add(Box.createVerticalStrut(10));
                    chatPanel.revalidate();
                    chatPanel.repaint();

                    inputField.setText("");

                    SwingUtilities.invokeLater(() -> {
                        JScrollBar vertical = scrollPane.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });

                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(chatPrivado,
                            "Error enviando mensaje: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        sendButton.addActionListener(e -> enviarMensajePrivado.run());
        inputField.addActionListener(e -> enviarMensajePrivado.run());

        chatPrivado.setVisible(true);
    }

    private void agregarMensajeAlChat(String emisor, String mensaje, boolean esPrivado) {
        MensajePanel mensajePanel = new MensajePanel(emisor, mensaje);
        if (esPrivado) {
            mensajePanel.setBackground(new Color(255, 255, 200)); // Highlight private messages
        }
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
        JBChatDirecto.addActionListener(e -> iniciarChatDirecto());
    }

    private void enviarMensaje() {
        String mensaje = JTFMessgeInput.getText().trim();

        if (mensaje.isEmpty()) return;

        try {
            if (servidor != null) {
                servidor.enviarMensaje(client.getName(), mensaje);
                JTFMessgeInput.setText("");
            } else {
                 JOptionPane.showMessageDialog(this,
                    "No estás conectado al servidor.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Error enviando mensaje: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void start() {
        this.setTitle("Chat - " + client.getName() + " [Puerto: " + client.getPort() + "]");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(JPMain);
        
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