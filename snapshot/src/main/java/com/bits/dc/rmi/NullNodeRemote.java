package com.bits.dc.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.bits.dc.model.Node;

/**
 * Used to simulate crashed node or in case of network errors
 */
public final class NullNodeRemote extends UnicastRemoteObject implements IServer {

    
    private final Node node;

    public NullNodeRemote(Node node) throws RemoteException {
        this.node = node;
    }

    
    @Override
    public Node getNode() throws RemoteException {
        return node;
    }

    @Override
    public void addNode(int id, String host) throws RemoteException {
    }

    @Override
    public void transferMoney(int recipientNodeId, int amount) throws RemoteException {
    }

    @Override
    public boolean acceptMoney(int senderNodeId, int amount) throws RemoteException {
        return false;
    }

    @Override
    public void receiveMarker(int nodeId) throws RemoteException {
    }
}
