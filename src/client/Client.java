package client;

import UI.Chat;

import javax.swing.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.util.Enumeration;

public class Client {
    private final String name;
    private final String ip;
    private final int port;

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

    public static String obtenerIPLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaz = interfaces.nextElement();

                // Saltar interfaces inactivas o loopback
                if (interfaz.isLoopback() || !interfaz.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> direcciones = interfaz.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress addr = direcciones.nextElement();

                    // Filtrar solo IPv4 y evitar loopback (127.0.0.1)
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

            Client client= new Client(InetAddress.getLocalHost().getHostName(),
                    obtenerIPLocal(),5000);

            System.out.println("client.Client info:\n" +
                    client.name+"\n"+client.ip+"\n"+client.port
            );
            Chat chat = new Chat(client);
            SwingUtilities.invokeLater(()-> chat.setVisible(true));
        }catch (RemoteException | UnknownHostException e){
            JOptionPane.showMessageDialog(null,
                    "Something went wrong\n"+e.getMessage(),
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}
