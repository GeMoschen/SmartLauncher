package de.gemo.smartlauncher.units;

import java.io.File;
import java.util.ArrayList;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class Library {
    public static int libCount = 0;
    private static ArrayList<Library> libraryDownloadList = new ArrayList<Library>(), libraryList = new ArrayList<Library>();

    private String path, name, version, nativesWin, fullPath;
    private boolean allow = true;
    private boolean extract = false;
    private ArrayList<String> extractExcludes = new ArrayList<String>();

    private String URL = VARS.URL.FILES.LIBRARIES;
    private String nameAppend = "";

    public Library(JsonObject object) throws Exception {
        try {
            this.initialize(object.get("name").asString());
            this.checkNatives(object);
            this.checkExtract(object);
            this.checkRules(object);
            this.checkNameAppend(object);
            // TODO: handle nameAppendix...
            this.checkURL(object);
        } catch (Exception e) {
            throw new Exception("Library is invalid!\n" + e.getMessage());
        }
    }

    private void checkNameAppend(JsonObject object) {
        JsonValue jsonValue = object.get("nameappend");
        if (jsonValue != null) {
            this.nameAppend = jsonValue.asString();
            this.fullPath = this.path + "/" + this.name + "/" + this.version + "/" + this.getFileName();
        }
    }

    private void checkURL(JsonObject object) {
        JsonValue jsonValue = object.get("url");
        if (jsonValue != null) {
            this.URL = jsonValue.asString();
            if (!this.URL.endsWith("/")) {
                this.URL += "/";
            }
        }
    }

    public Library(String fullString) throws Exception {
        this.initialize(fullString);
    }

    private void checkRules(JsonObject object) {
        JsonValue jsonValue = object.get("rules");
        if (jsonValue != null) {
            JsonArray jsonArray = jsonValue.asArray();
            boolean allowWindows = false;

            for (JsonValue val : jsonArray.values()) {
                JsonObject obj = val.asObject();
                boolean allow = !obj.get("action").asString().contains("disallow");
                JsonValue osValue = obj.get("os");
                if (allow) {
                    if (osValue == null) {
                        allowWindows = true;
                    } else {
                        JsonObject osObj = osValue.asObject();
                        if (osObj.get("name").asString().contains("windows")) {
                            allowWindows = true;
                        }
                    }
                } else {
                    if (osValue != null) {
                        JsonObject osObj = osValue.asObject();
                        if (osObj.get("name").asString().contains("windows")) {
                            allowWindows = false;
                        }
                    } else {
                        allowWindows = false;
                    }
                }
                if (allowWindows == false) {
                    this.setAllow(allowWindows);
                    break;
                }
            }
        }
    }

    private void checkExtract(JsonObject object) {
        JsonValue jsonValue = object.get("extract");
        if (jsonValue != null) {
            JsonObject jsonObject = jsonValue.asObject();
            jsonValue = jsonObject.get("exclude");
            if (jsonValue != null) {
                JsonArray jsonArray = jsonValue.asArray();
                for (JsonValue value : jsonArray) {
                    this.addExtractExclude(value.asString());
                }
            }
            this.setExtract(true);
        } else {
            this.setExtract(false);
        }
    }

    private void checkNatives(JsonObject object) {
        JsonValue jsonValue = object.get("natives");
        if (jsonValue != null) {
            JsonObject jsonObject = jsonValue.asObject();
            JsonValue nativeWindows = jsonObject.get("windows");
            if (nativeWindows != null) {
                String str_win = nativeWindows.asString();
                if (str_win != null) {
                    this.setNative(str_win);
                }
            }
        }
    }

    public void initialize(String fullString) {
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
            return this.name + "-" + this.version + this.nameAppend + ".jar";
        else
            return this.name + "-" + this.version + this.nameAppend + "-" + this.nativesWin + ".jar";
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
        if (this.isAllow() && !file.exists()) {
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

    public String getURL() {
        return URL;
    }
}
