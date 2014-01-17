package de.gemo.smartlauncher.listener;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.core.Main;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.units.Library;

public class MCDownloadFileListener extends HTTPListener {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0,00");

    private String fileName;

    public MCDownloadFileListener(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setText("downloading '" + this.fileName + "'...");
        StatusFrame.INSTANCE.setProgress(0);
    }

    @Override
    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.onError(action);
            return;
        }

        try {
            StatusFrame.INSTANCE.setText("download finished...");
            // TODO: CHECK ASSETS & LIBRARIES
            // if (Library.getLibraryDownloadList().size() < 1 &&
            // Asset.getAssetsToLoad() < 1) {
            // Logger.info("No libraries/assets to download...");
            // Launcher.startGame();
            // StatusFrame.INSTANCE.showGUI(false);
            // MainFrame.CORE.showFrame(true);
            // }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        Library.clearLibrarys();
        Main.clearHTTPs();
        JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
        StatusFrame.INSTANCE.showGUI(false);
        MainFrame.CORE.showFrame(true);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = Float.valueOf(decimalFormat.format((float) (((float) currentLength / (float) maximumLength) * 10000f)));
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
