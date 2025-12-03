package client;

import UI.Chat;

import javax.swing.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

public class Client {
    private final String name;
    private final String ip;
    private final int port;
    private Registry registryLocal;

    public Client(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void crearRegistryLocal(RemoteChat chatInterface) {
        try {
            // Crear registry en puerto único para este cliente
            registryLocal = LocateRegistry.createRegistry(port);

            // Exportar el objeto del chat
            RemoteChat stub = (RemoteChat) UnicastRemoteObject.exportObject(chatInterface, 0);

            // Registrar en el registry local
            registryLocal.rebind("ChatCliente_" + name, stub);

            System.out.println("Cliente " + name + " escuchando en puerto " + port);

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null,
                    "Error creando registry local: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String obtenerIPLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaz = interfaces.nextElement();

                if (interfaz.isLoopback() || !interfaz.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> direcciones = interfaz.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress addr = direcciones.nextElement();

                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "No se pudo obtener la IP";
    }

    public static void main(String[] args) {
        try {
            // Pedir puerto al usuario
            String puertoStr = JOptionPane.showInputDialog(
                    null,
                    "Ingresa el puerto para tu cliente (ej: 5000, 5001, 5002):",
                    "Puerto del Cliente",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (puertoStr == null || puertoStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debes ingresar un puerto");
                System.exit(0);
                return;
            }

            int puerto = Integer.parseInt(puertoStr.trim());

            Client client = new Client(
                    InetAddress.getLocalHost().getHostName(),
                    obtenerIPLocal(),
                    puerto
            );

            System.out.println("Client info:\n" +
                    "Nombre: " + client.name + "\n" +
                    "IP: " + client.ip + "\n" +
                    "Puerto: " + client.port
            );

            Chat chat = new Chat(client);
            SwingUtilities.invokeLater(() -> chat.setVisible(true));

        } catch (RemoteException | UnknownHostException e) {
            JOptionPane.showMessageDialog(null,
                    "Something went wrong\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Puerto inválido",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}