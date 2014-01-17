package de.gemo.smartlauncher.units;

import java.io.File;
import java.util.ArrayList;

public class Library {
    public static int libCount = 0;
    private static ArrayList<Library> libraryDownloadList = new ArrayList<Library>(), libraryList = new ArrayList<Library>();

    private String path, name, version, nativesWin, fullPath;
    private boolean allow = true;
    private boolean extract = false;
    private ArrayList<String> extractExcludes = new ArrayList<String>();

    public Library(String fullString) throws Exception {
        String split[] = fullString.split(":");
        this.path = split[0].replaceAll("\\.", "/");
        this.name = split[1];
        this.version = split[2];
        this.nativesWin = "";
        this.fullPath = this.path + "/" + this.name + "/" + this.version + "/" + this.getFileName();
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getFileName() {
        if (this.nativesWin.length() < 1)
            return this.name + "-" + this.version + ".jar";
        else
            return this.name + "-" + this.version + "-" + this.nativesWin + ".jar";
    }

    public void setNative(String nativesWin) {
        nativesWin = nativesWin.replace("${arch}", System.getProperty("sun.arch.data.model"));
        this.nativesWin = nativesWin;
        this.fullPath = this.path + "/" + this.name + "/" + this.version + "/" + this.getFileName();
    }

    public static boolean incrementCount() {
        libCount++;
        return libraryDownloadList.size() == libCount;
    }

    public static void clearLibrarys() {
        libCount = 0;
        libraryDownloadList.clear();
    }

    public void setAllow(boolean allow) {
        this.allow = allow;
    }

    public boolean isAllow() {
        return allow;
    }

    public String getNativesWin() {
        return nativesWin;
    }

    public boolean addLibraryToDownloads() {
        File file = new File(this.getDir(), this.getFileName());
        if (!file.exists()) {
            libraryDownloadList.add(this);
            return true;
        }
        return false;
    }

    public boolean addLibrary() {
        if (this.isAllow()) {
            libraryList.add(this);
            return true;
        }
        return false;
    }

    public void setExtract(boolean extract) {
        this.extract = extract;
    }

    public void addExtractExclude(String exclude) {
        this.extractExcludes.add(exclude.replaceAll("\"", ""));
    }

    public boolean isExtract() {
        return extract;
    }

    public static ArrayList<Library> getLibraryList() {
        return libraryList;
    }

    public static ArrayList<Library> getLibraryDownloadList() {
        return libraryDownloadList;
    }

    public String getDir() {
        return VARS.DIR.LIBRARIES + "/" + this.getPath() + "/" + this.getName() + "/" + this.getVersion() + "/";
    }

    public String getStartFile() {
        return this.path + "\\" + this.getName() + "\\" + this.getVersion() + "\\" + this.getFileName();
    }

    public boolean shouldExtract(String name) {
        for (String string : this.extractExcludes) {
            if (string.equalsIgnoreCase(name) || name.startsWith(string)) {
                return false;
            }
        }
        return true;
    }
}
