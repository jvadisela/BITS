package com.bits.dc.model;

import java.io.Serializable;

import com.google.common.base.MoreObjects;

public final class Account implements Serializable {

	private static final long serialVersionUID = 690809856446768714L;
	private int balance;
    private int withdrawAmount;

    public Account(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void incrementBalance(int amount) {
        balance += amount;
    }

    public void restoreBalance() {
        balance += withdrawAmount;
        withdrawAmount = 0;
    }

    public boolean decrementBalance(int amount) {
        if (balance >= amount) {
            balance -= amount;
            withdrawAmount = amount;
            return true;
        }
        return false;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + balance;
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
		Account other = (Account) obj;
		if (balance != other.balance)
			return false;
		return true;
	}

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("balance", balance)
                .toString();
    }
}
