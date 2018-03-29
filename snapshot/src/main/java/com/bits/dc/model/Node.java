package com.bits.dc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.bits.dc.Constants;
import com.bits.dc.utils.SnapshotPersistenceUtil;
import com.google.common.base.MoreObjects;

public final class Node implements Serializable {

    private final String id;

    private final String host;

    private final Account item;

    private final Snapshot snapshot;

    private final Map<String, String> nodes = new HashMap<>();

    public Node(String id, String host) {
        this.id = id;
        this.host = host;
        item = new Account(Constants.INITIAL_BALANCE);
        snapshot = new Snapshot();
        nodes.put(id, host);
    }

    public String getId() {
        return id;
    }

    
    public Account getItem() {
        return item;
    }

    
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Starts distributed snapshot by capturing local balance and waiting for marker from other nodes
     */
    public void startSnapshotRecording() {
        snapshot.startSnapshotRecording(id, item.getBalance(), nodes);
    }

    public void stopSnapshotRecording() {
        SnapshotPersistenceUtil.write(this);
        snapshot.stopSnapshotRecording();
    }

    public void putNodes(Map<String, String> nodes) {
        this.nodes.putAll(nodes);
    }

    public void putNode(String id, String host) {
        nodes.put(id, host);
    }

    
    public String getHost() {
        return host;
    }

    public Map<String, String> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Node) {
            Node object = (Node) o;

            return Objects.equals(id, object.id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("host", host)
                .add("item", item)
                .add("snapshot", snapshot)
                .add("nodes", Arrays.toString(nodes.entrySet().toArray()))
                .toString();
    }
}
