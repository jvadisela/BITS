package com.bits.dc.utils;

import java.rmi.Naming;
import java.rmi.RemoteException;

import com.bits.dc.model.Node;
import com.bits.dc.rmi.IServer;
import com.bits.dc.rmi.NullNodeRemote;

/**
 * Convenient class to deal with RMI for nodes
 */
public abstract class RMIUtils {

    /**
     * Get reference to remote node
     *
     * @param node remote node
     * @return reference to remote object
     */
    
    public static IServer getRemoteNode(Node node) {
        return getRemoteNode(node.getId(), node.getHost());
    }

    /**
     * Get reference to remote node
     *
     * @param id   of the node
     * @param host of the node
     * @return reference to remote object
     */
    
    public static IServer getRemoteNode(int id, String host) {
        try {
            return (IServer) Naming.lookup("rmi://" + host + "/NodeRemote" + id);
        } catch (Exception e) {
        	System.out.println("Failed to get remote interface for id=" + id);
        	e.printStackTrace();
            try {
                return new NullNodeRemote(new Node());
            } catch (RemoteException re) {
            	System.out.println("Failed to get Null Node Pattern");
            	re.printStackTrace();
                throw new RuntimeException("RMI failed miserably", re);
            }
        }
    }
}
