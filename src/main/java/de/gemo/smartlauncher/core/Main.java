package de.gemo.smartlauncher.core;

import javax.swing.UIManager;

import de.gemo.smartlauncher.actions.RefreshAction;
import de.gemo.smartlauncher.frames.LoginFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPThread;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.listener.LoginListener;
import de.gemo.smartlauncher.units.AuthData;

public class Main {

    // Threads
    private static Thread thread;
    private static HTTPThread httpThread;

    public static AuthData authData = new AuthData();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // refresh login...
        refreshLogin();
    }

    private static void refreshLogin() {
        if (authData.load()) {
            new StatusFrame();
            appendWorker(new Worker(new RefreshAction(authData), new LoginListener()));
            startThread();
        } else {
            new LoginFrame(200, 190);
        }
    }

    public synchronized static void createThreads() {
        if (thread == null) {
            httpThread = new HTTPThread();
            thread = new Thread(httpThread);
        }
    }

    public synchronized static void appendWorker(Worker worker) {
        createThreads();
        httpThread.appendWorker(worker);
    }

    public synchronized static void startThread() {
        createThreads();
        if (thread.getState() == Thread.State.TERMINATED) {
            thread = new Thread(httpThread);
            thread.start();
            return;
        }
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
            return;
        }
    }

    public static void clearHTTPs() {
        createThreads();
        httpThread.clear();
    }

}
