package de.gemo.smartlauncher.launcher.core;

import de.gemo.smartlauncher.universal.frames.LogFrame;

public class Logger {
    public static void fine(String message) {
        print("[ FINE ]", message);
    }

    public static void info(String message) {
        print("[ INFO ]", message);
    }

    public static void error(String message) {
        print("[ ERROR ]", message);
    }

    public static void warning(String message) {
        print("[ WARNING ]", message);
    }

    public static void client(String message) {
        print("[ CLIENT ]", message);
    }

    public static void print(String status, String message) {
        String completeMessage = status + " " + message;
        if (LogFrame.INSTANCE != null) {
            LogFrame.INSTANCE.appendText(completeMessage);
        } else {
            System.out.println(completeMessage);
        }
    }
}
