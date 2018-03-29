package com.bits.dc.utils;

import java.rmi.Naming;

import com.bits.dc.model.Node;
import com.bits.dc.rmi.IServer;

public abstract class RMIUtils {

    public static IServer getRemoteNode(Node node) {
        return getRemoteNode(node.getId(), node.getHost());
    }

    public static IServer getRemoteNode(int id, String host) {
        try {
            return (IServer) Naming.lookup("rmi://" + host + "/NodeRemote" + id);
        } catch (Exception e) {
        	System.out.println("unable to get remote node for id = " + id);
        	e.printStackTrace();
            throw new RuntimeException("unable to get remote node for  : ", e);
        }
    }
}
