package de.gemo.smartlauncher.core;

public class Logger {
    public static void info(String message) {
        print("[ INFO ]", message);
    }

    public static void error(String message) {
        print("[ ERROR ]", message);
    }

    public static void warning(String message) {
        print("[ WARNING ]", message);
    }

    public static void print(String status, String message) {
        System.out.println(status + " " + message);
    }

}
