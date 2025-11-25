package server;

import client.Client;
import client.RemoteChat;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServerChat implements ServerChatInterface {
    private List<RemoteChat> clientesConectados;

    public ServerChat() {
        this.clientesConectados = new ArrayList<>();
    }

    @Override
    public synchronized void registrarCliente(RemoteChat cliente) throws RemoteException {
        clientesConectados.add(cliente);
        String nombre = cliente.getNombre();
        System.out.println("Cliente conectado: " + nombre);

        // Notificar a todos que se unió alguien
        enviarMensaje("Sistema", nombre + " se ha unido al chat");
    }

    @Override
    public synchronized void desregistrarCliente(RemoteChat cliente) throws RemoteException {
        String nombre = cliente.getNombre();
        clientesConectados.remove(cliente);
        System.out.println("Cliente desconectado: " + nombre);

        // Notificar a todos que alguien se fue
        enviarMensaje("Sistema", nombre + " ha salido del chat");
    }

    @Override
    public synchronized void enviarMensaje(String emisor, String mensaje) throws RemoteException {
        System.out.println("[" + emisor + "]: " + mensaje);

        // Enviar el mensaje a todos los clientes conectados
        List<RemoteChat> clientesDesconectados = new ArrayList<>();

        for (RemoteChat cliente : clientesConectados) {
            try {
                cliente.getMessage(emisor, mensaje);
            } catch (RemoteException e) {
                // Si falla, marcar para remover
                clientesDesconectados.add(cliente);
                System.err.println("Error enviando mensaje a cliente: " + e.getMessage());
            }
        }

        // Remover clientes desconectados
        clientesConectados.removeAll(clientesDesconectados);
    }

    public static void main(String[] args) {
        try {
            String ip = Client.obtenerIPLocal();
            System.setProperty("java.rmi.server.hostname", ip);

            ServerChat servidor = new ServerChat();
            ServerChatInterface stub = (ServerChat) UnicastRemoteObject.exportObject(servidor, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ChatServidor", stub);

            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║   SERVIDOR DE CHAT INICIADO       ║");
            System.out.println("║   IP: " + ip + "                  ║");
            System.out.println("║   Puerto: 1099                     ║");
            System.out.println("╚════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}