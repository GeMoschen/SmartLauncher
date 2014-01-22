package de.gemo.smartlauncher.launcher.units;

public class PackVersion {

    private String version;

    public PackVersion(String version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return (this.version).hashCode();
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PackVersion) {
            PackVersion version = (PackVersion) obj;
            return this.version.equalsIgnoreCase(version.version);
        }
        return false;
    }
}
