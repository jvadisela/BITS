package com.bits.dc.rmi;

import org.jetbrains.annotations.NotNull;

import com.bits.dc.model.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to be used by SERVER for accessing the remote node via RMI
 */
public interface NodeServer extends Remote {

    @NotNull Node getNode() throws RemoteException;

    void addNode(int id, @NotNull String host) throws RemoteException;

    void transferMoney(int recipientNodeId, int amount) throws RemoteException;

    boolean acceptMoney(int senderNodeId, int amount) throws RemoteException;

    void receiveMarker(int nodeId) throws RemoteException;
}
