package de.gemo.smartlauncher.universal.units;

import de.gemo.smartlauncher.universal.internet.HTTPThread;
import de.gemo.smartlauncher.universal.internet.Worker;

public class ThreadHolder {

    // Threads
    private static Thread thread;
    private static HTTPThread httpThread;

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
