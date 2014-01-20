package de.gemo.smartlauncher.core.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.frames.StatusFrame;

public class MinecraftMonitorThread extends Thread {

    private final MinecraftProcess process;
    private boolean firstLine = true;

    public MinecraftMonitorThread(MinecraftProcess process) {
        super("MinecraftMonitorThread");
        this.process = process;
    }

    public void run() {
        InputStreamReader reader = new InputStreamReader(this.process.getProcess().getInputStream());
        BufferedReader buf = new BufferedReader(reader);
        String line = null;
        try {
            while ((line = buf.readLine()) != null) {
                Logger.client(line);
                if (firstLine) {
                    StatusFrame.INSTANCE.showFrame(false);
                    firstLine = false;
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                buf.close();
            } catch (IOException e) {
            } finally {
                try {
                    this.process.getProcess().waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.process.getExitListener() != null) {
            this.process.getExitListener().onExit(this.process);
        }
    }
}
