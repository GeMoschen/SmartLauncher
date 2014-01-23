package de.gemo.smartlauncher.launcher.listener;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.imageio.ImageIO;

import de.gemo.smartlauncher.launcher.frames.LoginFrame;
import de.gemo.smartlauncher.launcher.frames.MainFrame;
import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.ByteResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.units.Logger;

public class GetPackIconListener extends HTTPListener {

    private static int count = 0;
    private final Pack pack;

    public GetPackIconListener(Pack pack) {
        this.pack = pack;
    }

    public void onStart(HTTPAction action) {
        Logger.info("Getting icon for '" + this.pack.getPackName() + "'...");
    }

    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() == HttpURLConnection.HTTP_OK) {
            ByteResponse response = (ByteResponse) this.getWorker().getResponse();
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.getResponse()));
                this.pack.setIcon(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.fine("Icon received...");
        } else {
            Logger.warning("No icon found. Using standardicon...");
        }
        count++;

        if (count == Pack.loadedPacks.size()) {
            new MainFrame("SmartLauncher", 300, 400);
            if (LoginFrame.INSTANCE != null) {
                LoginFrame.INSTANCE.showGUI(false);
            }
            if (StatusFrame.INSTANCE != null) {
                StatusFrame.INSTANCE.showFrame(false);
            }
        }

    }

    @Override
    public void onError(HTTPAction action) {
        // do nothing...
        Logger.warning("Error fetching icon...");
        count++;
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        // do nothing...
    }

}
