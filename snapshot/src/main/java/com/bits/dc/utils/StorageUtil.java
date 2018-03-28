package com.bits.dc.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.bits.dc.model.Node;
import com.bits.dc.model.Snapshot;

/**
 * Convenient class to work with Node's internal list of snapshots
 * <p>
 * Maintains CSV file (under STORAGE_FOLDER directory) in format:
 * {snapshotID},{local balance},{sum of all incoming transfers upon receiving the marker}
 */
public abstract class StorageUtil {

    private static final String SEPARATOR = ",";
    private static final String STORAGE_FOLDER = "storage";

    /**
     * Creates/Updates list of nodes snapshots into CSV file
     *
     * @param node to write
     */
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

    /**
     * Creates storage folder to keep node's CSV files in
     */
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

    /**
     * Removes node's CSV file
     *
     * @param nodeId of the node
     */
    public static void removeFile(int nodeId) {
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

    
    private static String getFileName(int nodeId) {
        return STORAGE_FOLDER + "/Node-" + nodeId + ".csv";
    }
}
