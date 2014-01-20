package de.gemo.smartlauncher.core;

import de.gemo.smartlauncher.frames.MainFrame;

public class MinecraftListener {

    public void onExit(MinecraftProcess process) {
        Logger.fine("Minecraft closed! ( Exitvalue: " + process.getProcess().exitValue() + " ) ");
        MainFrame.CORE.showFrame(true);
    }
}
