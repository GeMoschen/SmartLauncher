package de.gemo.smartlauncher.core.minecraft;

import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;

public class MinecraftListener {

    public void onExit(MinecraftProcess process) {
        Logger.fine("Minecraft closed! ( Exitvalue: " + process.getProcess().exitValue() + " ) ");
        StatusFrame.INSTANCE.showFrame(false);
        MainFrame.INSTANCE.showFrame(true);
    }
}
