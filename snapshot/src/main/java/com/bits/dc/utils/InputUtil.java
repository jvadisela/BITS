package com.bits.dc.utils;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public abstract class InputUtil {

    private static final String SEPARATOR = ",";

    private static final Pattern INTEGER = Pattern.compile("^-?\\d+$");

    public static void readInput(String className) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String[] commands = scanner.nextLine().split(SEPARATOR);
            Object[] params = new Object[commands.length - 1];
            Class<?>[] methodParameterTypes = new Class<?>[commands.length - 1];
            for (int i = 1; i < commands.length; i++) {
                int param = i - 1;
                params[param] = commands[i];
                methodParameterTypes[param] = String.class;
            }
            System.out.println("Calling method=" + commands[0] + Arrays.toString(params));
            try {
                Class.forName(className).getMethod(commands[0], methodParameterTypes).invoke(null, params);
            } catch (Exception e) {
            	System.out.println("Input scanner error");
            	e.printStackTrace();
            }
        }
    }
}
