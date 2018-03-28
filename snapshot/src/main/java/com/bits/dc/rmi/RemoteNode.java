package com.bits.dc.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.bits.dc.model.Node;
import com.bits.dc.model.Snapshot;
import com.bits.dc.utils.RMIUtils;

public final class RemoteNode extends UnicastRemoteObject implements IServer {

	private static final long serialVersionUID = 3871533811290162457L;

    private static final ReadWriteLock nodesLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock itemTransferLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock itemAcceptLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock markerLock = new ReentrantReadWriteLock();

    
    private final Node node;

    public RemoteNode(Node node) throws RemoteException {
        this.node = node;
    }

    
    @Override
    public Node getNode() throws RemoteException {
        System.out.println("Get node=" + node);
        return node;
    }

    @Override
    public void addNode(int id, String host) throws RemoteException {
        nodesLock.writeLock().lock();
        try {
            System.out.println("Add id=" + id + ", host=" + host);
            node.putNode(id, host);
            System.out.println("Current nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
        } finally {
            nodesLock.writeLock().unlock();
        }
    }

    @Override
    public void transferMoney(int recipientNodeId, int amount) throws RemoteException {
        itemTransferLock.writeLock().lock();
        try {
            boolean isWithdraw = node.getItem().decrementBalance(amount);
            if (isWithdraw) {
                System.out.println("Transferring amount=" + amount + " to recipientNodeId=" + recipientNodeId);
                boolean isAccepted = RMIUtils.getRemoteNode(recipientNodeId, node.getNodes().get(recipientNodeId)).acceptMoney(node.getId(), amount);
                if (isAccepted) {
                    System.out.println("Transferred amount=" + amount + " to recipientNodeId=" + recipientNodeId);
                } else {
                    node.getItem().restoreBalance();
                    System.out.println("NOT Transferred amount=" + amount + " to recipientNodeId=" + recipientNodeId);
                }
            } else {
                System.out.println("NOT Withdraw money amount=" + amount);
            }
        } finally {
            itemTransferLock.writeLock().unlock();
        }
    }

    @Override
    public boolean acceptMoney(int senderNodeId, int amount) throws RemoteException {
        itemAcceptLock.writeLock().lock();
        try {
            System.out.println("Accepting money amount=" + amount + " from senderNodeId=" + senderNodeId);
            node.getSnapshot().incrementMoneyInTransfer(senderNodeId, amount);
            node.getItem().incrementBalance(amount);
            System.out.println("Accepted, new balance=" + node.getItem().getBalance());
            return true;
        } finally {
            itemAcceptLock.writeLock().unlock();
        }
    }

    @Override
    public void receiveMarker(int nodeId) throws RemoteException {
        markerLock.writeLock().lock();
        itemAcceptLock.writeLock().lock();
        itemTransferLock.writeLock().lock();
        try {
            System.out.println("Received marker from nodeId=" + nodeId);
            Snapshot snapshot = node.getSnapshot();
            if (!snapshot.isRecording()) {
                node.startSnapshotRecording();
                System.out.println("Broadcasting marker to neighbours");
                ExecutorService executorService = Executors.newFixedThreadPool(node.getNodes().size() - 1);
                node.getNodes().entrySet().parallelStream().filter(n -> n.getKey() != node.getId()).forEach(entry -> {
                    executorService.execute(() -> {
                        try {
                            RMIUtils.getRemoteNode(entry.getKey(), entry.getValue()).receiveMarker(node.getId());
                            System.out.println("Marker sent to nodeId=" + entry.getKey());
                        } catch (RemoteException e) {
                            System.out.println("Failed to sent marker to nodeId=" + entry.getKey());
                            e.printStackTrace();
                        }
                    });
                });
            }
            snapshot.stopRecording(nodeId);
            if (!snapshot.isRecording()) {
                System.out.println("Received all markers for snapshot on nodeId=" + nodeId);
                node.stopSnapshotRecording();
            }
        } finally {
            markerLock.writeLock().unlock();
            itemAcceptLock.writeLock().unlock();
            itemTransferLock.writeLock().unlock();
        }
    }
}
