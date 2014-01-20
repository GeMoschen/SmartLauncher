package de.gemo.smartlauncher.core.minecraft;

public class MinecraftProcess {
    private final Process process;
    private MinecraftListener exitListener;
    private final MinecraftMonitorThread monitorThread;

    public MinecraftProcess(Process process) {
        this.process = process;
        this.monitorThread = new MinecraftMonitorThread(this);
        this.monitorThread.start();
        this.exitListener = new MinecraftListener();
    }

    public MinecraftListener getExitListener() {
        return this.exitListener;
    }

    public Process getProcess() {
        return this.process;
    }
}
