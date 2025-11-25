package server;

import client.RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerChatInterface extends Remote {
    void registrarCliente(RemoteChat cliente) throws RemoteException;
    void desregistrarCliente(RemoteChat cliente) throws RemoteException;
    void enviarMensaje(String emisor, String mensaje) throws RemoteException;
}