package com.bits.dc.model;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class Item implements Serializable {

    private int balance;

    private int withdrawAmount;

    public Item(int balance) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Item) {
            Item object = (Item) o;

            return Objects.equals(balance, object.balance);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(balance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("balance", balance)
                .toString();
    }
}
