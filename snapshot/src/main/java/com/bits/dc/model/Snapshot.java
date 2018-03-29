package com.bits.dc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public final class Snapshot implements Serializable {

	private static final long serialVersionUID = 1519821847950264078L;
	private int id;
    private int localBalance;
    private int moneyInTransfer;
    private final Set<String> unrecordedChannels = new HashSet<>();

    public void startSnapshotRecording(String nodeId, int balance, Map<String, String> nodes) {
        id++;
        localBalance = balance;
        moneyInTransfer = 0;
        unrecordedChannels.addAll(nodes.entrySet().parallelStream().filter(n -> !n.getKey().equals(nodeId)).map(Map.Entry::getKey).collect(Collectors.toSet()));
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

    public void incrementMoneyInTransfer(String recipientNodeId, int amount) {
        if (unrecordedChannels.contains(recipientNodeId)) {
            moneyInTransfer += amount;
        }
    }

    public void stopRecording(String nodeId) {
        unrecordedChannels.remove(nodeId);
    }

    public boolean isRecording() {
        return unrecordedChannels.size() != 0;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + localBalance;
		result = prime * result + moneyInTransfer;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Snapshot other = (Snapshot) obj;
		if (id != other.id)
			return false;
		if (localBalance != other.localBalance)
			return false;
		if (moneyInTransfer != other.moneyInTransfer)
			return false;
		return true;
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
