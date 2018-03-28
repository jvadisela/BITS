package com.bits.dc.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.bits.dc.model.Node;

public interface IServer extends Remote {

    Node getNode() throws RemoteException;

    void addNode(int id, String host) throws RemoteException;

    void transferMoney(int recipientNodeId, int amount) throws RemoteException;

    boolean acceptMoney(int senderNodeId, int amount) throws RemoteException;

    void receiveMarker(int nodeId) throws RemoteException;
}
