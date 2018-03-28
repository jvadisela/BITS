package com.bits.dc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class ServiceConfiguration {

    public static final String CONFIGURATION_FILE = "service.properties";

    private static int rmiPort;

    private static int bankInitialAmount;

    private static int bankTransferMinAmount;

    private static int bankTransferMaxAmount;

    private static int bankTransferTimeoutFrequency;

    private static String bankTransferTimeoutUnit;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(CONFIGURATION_FILE));
            rmiPort = Integer.parseInt(properties.getProperty("rmi-port"));
            bankInitialAmount = Integer.parseInt(properties.getProperty("bank-initial-amount"));
            bankTransferMinAmount = Integer.parseInt(properties.getProperty("bank-transfer-min-amount"));
            bankTransferMaxAmount = Integer.parseInt(properties.getProperty("bank-transfer-max-amount"));
            bankTransferTimeoutFrequency = Integer.parseInt(properties.getProperty("bank-transfer-timeout-frequency"));
            bankTransferTimeoutUnit = properties.getProperty("bank-transfer-timeout-unit");
        } catch (IOException e) {
        	System.out.println("Failed to load service configuration!");
        	e.printStackTrace();
        }
    }

    public static int getRmiPort() {
        return rmiPort;
    }

    public static int getBankInitialAmount() {
        return bankInitialAmount;
    }

    public static int getBankTransferMinAmount() {
        return bankTransferMinAmount;
    }

    public static int getBankTransferMaxAmount() {
        return bankTransferMaxAmount;
    }

    public static int getBankTransferTimeoutFrequency() {
        return bankTransferTimeoutFrequency;
    }

    public static String getBankTransferTimeoutUnit() {
        return bankTransferTimeoutUnit;
    }
}
