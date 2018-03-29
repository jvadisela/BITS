package com.bits.dc.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.bits.dc.model.Node;
import com.bits.dc.model.Snapshot;

public abstract class SnapshotPersistenceUtil {

    private static final String SEPARATOR = ",";
    private static final String STORAGE_FOLDER = "storage";

    public static void write(Node node) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(getFileName(node.getId()), true)))) {
            Snapshot snapshot = node.getSnapshot();
            writer.println(snapshot.getId() + SEPARATOR + snapshot.getLocalBalance() + SEPARATOR + snapshot.getMoneyInTransfer());
            System.out.println("Storage wrote a snapshot=" + snapshot);
        } catch (Exception e) {
            System.out.println("Failed to write snapshot of node=" + node);
            e.printStackTrace();
        }
    }

    public static void init() {
        try {
            Path path = Paths.get(STORAGE_FOLDER);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (Exception e) {
            System.out.println("Failed to create storage directory");
            e.printStackTrace();

        }
    }

    public static void removeFile(String nodeId) {
        try {
            Path path = Paths.get(getFileName(nodeId));
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
        	System.out.println("Failed to remove file for nodeId=" + nodeId);
        	e.printStackTrace();
        }
    }

    
    private static String getFileName(String nodeId) {
        return STORAGE_FOLDER + "/Node-" + nodeId + ".csv";
    }
}
