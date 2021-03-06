package de.gemo.smartlauncher.launcher.core.minecraft;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.frames.MainFrame;
import de.gemo.smartlauncher.universal.frames.LogFrame;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.units.Logger;

public class MinecraftListener {

    public void onExit(MinecraftProcess process) {
        Logger.fine("Minecraft closed! ( Exitvalue: " + process.getProcess().exitValue() + " ) ");
        StatusFrame.INSTANCE.showFrame(false);
        MainFrame.INSTANCE.showFrame(true);
        if ((process.getProcess().exitValue() == 0) && !GameLauncher.isKeepConsoleOpen()) {
            LogFrame.close();
        }
    }
}
