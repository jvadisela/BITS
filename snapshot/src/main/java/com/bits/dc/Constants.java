package com.bits.dc;

public interface Constants {

    int INITIAL_BALANCE = ServiceConfiguration.getBankInitialAmount();
    int MIN_AMOUNT = ServiceConfiguration.getBankTransferMinAmount();
    int MAX_AMOUNT = ServiceConfiguration.getBankTransferMaxAmount();
    int TIMEOUT_FREQUENCY = ServiceConfiguration.getBankTransferTimeoutFrequency();
    String TIMEOUT_UNIT = ServiceConfiguration.getBankTransferTimeoutUnit();
}
