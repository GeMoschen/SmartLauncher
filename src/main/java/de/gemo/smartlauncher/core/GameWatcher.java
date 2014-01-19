package de.gemo.smartlauncher.core;

import de.gemo.smartlauncher.frames.MainFrame;

public class GameWatcher implements Runnable {

    private final Process gameProcess;

    public GameWatcher(Process gameProcess) {
        this.gameProcess = gameProcess;
    }

    @Override
    public void run() {
        while (this.isRunning()) {
            // sleep for 500ms...
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // some output...
        Logger.fine("Minecraft closed! ( Exitvalue: " + this.gameProcess.exitValue() + " ) ");

        // show GUIs
        MainFrame.CORE.showFrame(true);
    }

    private boolean isRunning() {
        try {
            this.gameProcess.exitValue();
        } catch (IllegalThreadStateException ex) {
            return true;
        }
        return false;
    }
}
