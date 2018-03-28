package com.bits.dc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public final class Snapshot implements Serializable {

    private int id;

    private int localBalance;

    private int moneyInTransfer;

    private final Set<Integer> unrecordedChannels = new HashSet<>();

    public void startSnapshotRecording(int nodeId, int balance, Map<Integer, String> nodes) {
        id++;
        localBalance = balance;
        moneyInTransfer = 0;
        unrecordedChannels.addAll(nodes.entrySet().parallelStream().filter(n -> n.getKey() != nodeId).map(Map.Entry::getKey).collect(Collectors.toSet()));
    }

    public void stopSnapshotRecording() {
        localBalance = 0;
        moneyInTransfer = 0;
        unrecordedChannels.clear();
    }

    public int getId() {
        return id;
    }

    public int getLocalBalance() {
        return localBalance;
    }

    public int getMoneyInTransfer() {
        return moneyInTransfer;
    }

    public void incrementMoneyInTransfer(int recipientNodeId, int amount) {
        if (unrecordedChannels.contains(recipientNodeId)) {
            moneyInTransfer += amount;
        }
    }

    public void stopRecording(int nodeId) {
        unrecordedChannels.remove(nodeId);
    }

    public boolean isRecording() {
        return unrecordedChannels.size() != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Snapshot) {
            Snapshot object = (Snapshot) o;

            return Objects.equals(id, object.id) &&
                    Objects.equals(localBalance, object.localBalance) &&
                    Objects.equals(moneyInTransfer, object.moneyInTransfer);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, localBalance, moneyInTransfer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("localBalance", localBalance)
                .add("moneyInTransfer", moneyInTransfer)
                .add("unrecordedChannels", Arrays.toString(unrecordedChannels.toArray()))
                .toString();
    }
}
