package client;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface RemoteChat extends Remote {
    void getMessage(String emisor, String mensaje) throws RemoteException;
    void getMensajePrivado(String emisor, String mensaje) throws RemoteException;
    String getNombre() throws RemoteException;
}