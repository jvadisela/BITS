package com.bits.dc.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.bits.dc.model.Node;

public interface IServer extends Remote {

    Node getNode() throws RemoteException;
    void addNode(String id, String host) throws RemoteException;
    void transferMoney(String recipientNodeId, int amount) throws RemoteException;
    boolean acceptMoney(String senderNodeId, int amount) throws RemoteException;
    void receiveMarker(String nodeId) throws RemoteException;
}
