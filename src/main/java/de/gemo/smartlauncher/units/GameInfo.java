package de.gemo.smartlauncher.units;

public class GameInfo {

    private final String gameVersion;

    private String assetVersion = "TEST";

    public GameInfo(String gameVersion) {
        this.gameVersion = gameVersion.replaceAll("\"", "");
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
    }

    public boolean isAssetsVirtual() {
        return assetVersion.equalsIgnoreCase("legacy");
    }

    public String getAssetVersion() {
        return assetVersion;
    }
}
