package de.gemo.smartlauncher.units;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.frames.MainFrame;

public class Pack {
    public static BufferedImage NO_ICON = null;

    static {
        // try {
        NO_ICON = new BufferedImage(MainFrame.IMAGE_DIM, MainFrame.IMAGE_DIM, BufferedImage.TYPE_INT_RGB);
        final int frame = 3;
        Color frameColor = new Color(89, 175, 200);
        for (int x = 0; x < MainFrame.IMAGE_DIM; x++) {
            for (int y = 0; y < MainFrame.IMAGE_DIM; y++) {
                if (x < frame || x > MainFrame.IMAGE_DIM - frame - 1 || y < frame || y > MainFrame.IMAGE_DIM - frame - 1 || x == y || MainFrame.IMAGE_DIM - x - 1 == y) {
                    NO_ICON.setRGB(x, y, frameColor.getRGB());
                } else {
                    NO_ICON.setRGB(x, y, Color.white.getRGB());
                }
            }
        }
    }

    private static int finishedPacks = 0, toLoadPacks = 0, errorPacks = 0;
    public static HashMap<String, Pack> loadedPacks = new HashMap<String, Pack>();

    private BufferedImage icon = NO_ICON;
    private String packName = null;
    private String packURL = null;
    private boolean isPrivate = false;
    private String permissionURL = null;
    private HashSet<PackVersion> versions = new HashSet<PackVersion>();

    public Pack(String packName) {
        this.packName = packName;
    }

    public void load(File file) throws IOException {
        if (!file.exists()) {
            this.packName = null;
            this.packURL = null;
            this.isPrivate = false;
            this.permissionURL = null;
            return;
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        JsonObject json = JsonObject.readFrom(reader);
        this.packName = file.getName().replace(".json", "");
        this.packURL = json.get("packURL").asString();
        this.isPrivate = json.get("isPrivate").asBoolean();
        this.permissionURL = json.get("permissionURL").asString();
        reader.close();
    }

    public void clearVersions() {
        this.versions.clear();
    }

    public void addVersion(PackVersion version) {
        if (!this.hasVersion(version)) {
            this.versions.add(version);
        }
    }

    public boolean hasVersion(String version) {
        return this.versions.contains(new PackVersion(version));
    }

    public boolean hasVersion(PackVersion version) {
        return this.versions.contains(version);
    }

    public boolean isValid() {
        return this.packName != null && this.packURL != null && (!this.isPrivate || (this.isPrivate && this.permissionURL != null));
    }

    public String getPackName() {
        return packName;
    }

    public HashSet<PackVersion> getVersions() {
        return versions;
    }

    public synchronized static int getFinishedPacks() {
        return finishedPacks;
    }

    public synchronized static void incrementFinishedPacks() {
        finishedPacks = finishedPacks + 1;
    }

    public synchronized static int getToLoadPacks() {
        return toLoadPacks;
    }

    public static boolean allPacksLoaded() {
        return Pack.getFinishedPacks() + Pack.getErrorPacks() >= Pack.getToLoadPacks();
    }

    public synchronized static void incrementErrorPacks() {
        errorPacks = errorPacks + 1;
    }

    public synchronized static int getErrorPacks() {
        return errorPacks;
    }

    public synchronized void setIcon(BufferedImage icon) {
        if (icon.getWidth() != MainFrame.IMAGE_DIM || icon.getHeight() != MainFrame.IMAGE_DIM) {
            return;
        }
        this.icon = icon;
    }

    public synchronized BufferedImage getIcon() {
        return icon;
    }

}
