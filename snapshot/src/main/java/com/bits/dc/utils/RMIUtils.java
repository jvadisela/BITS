package com.bits.dc.utils;

import java.rmi.Naming;
import java.rmi.RemoteException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.bits.dc.model.Node;
import com.bits.dc.rmi.IServer;
import com.bits.dc.rmi.NullNodeRemote;

/**
 * Convenient class to deal with RMI for nodes
 */
public abstract class RMIUtils {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Get reference to remote node
     *
     * @param node remote node
     * @return reference to remote object
     */
    @NotNull
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
    @NotNull
    public static IServer getRemoteNode(int id, String host) {
        try {
            return (IServer) Naming.lookup("rmi://" + host + "/NodeRemote" + id);
        } catch (Exception e) {
            logger.error("Failed to get remote interface for id=" + id, e);
            try {
                return new NullNodeRemote(new Node());
            } catch (RemoteException re) {
                logger.error("Failed to get Null Node Pattern", re);
                throw new RuntimeException("RMI failed miserably", re);
            }
        }
    }
}
