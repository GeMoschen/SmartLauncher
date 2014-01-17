package de.gemo.smartlauncher.units;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.eclipsesource.json.JsonObject;

public class AuthData {

    private String mcUserName = null, accessToken = null, clientToken = null, profileID = null;
    private boolean legacy = false;

    public synchronized String getMCUserName() {
        return mcUserName;
    }

    public synchronized void setMCUserName(String mcUserName) {
        if (mcUserName != null)
            mcUserName = mcUserName.replaceAll("\"", "");
        this.mcUserName = mcUserName;
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized void setAccessToken(String accessToken) {
        if (accessToken != null)
            accessToken = accessToken.replaceAll("\"", "");
        this.accessToken = accessToken;
    }

    public synchronized String getClientToken() {
        return clientToken;
    }

    public synchronized void setClientToken(String clientToken) {
        if (clientToken != null)
            clientToken = clientToken.replaceAll("\"", "");
        this.clientToken = clientToken;
    }

    public synchronized String getProfileID() {
        return profileID;
    }

    public synchronized void setProfileID(String profileID) {
        if (profileID != null)
            profileID = profileID.replaceAll("\"", "");
        this.profileID = profileID;
    }

    public synchronized boolean isLegacy() {
        return legacy;
    }

    public synchronized void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    public synchronized void resetData() {
        this.setMCUserName(null);
        this.setAccessToken(null);
        this.setClientToken(null);
        this.setProfileID(null);
        this.setLegacy(false);
    }

    public void save() throws IOException {
        File file = new File(VARS.DIR.APPDATA, "profile.json");
        if (file.exists()) {
            file.delete();
        }

        JsonObject json = new JsonObject();
        json.add("mcUserName", this.mcUserName);
        json.add("accessToken", this.accessToken);
        json.add("clientToken", this.clientToken);
        json.add("profileID", this.profileID);
        json.add("legacy", this.legacy);

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        json.writeTo(writer);
        writer.close();
    }

    public boolean load() {
        File file = new File(VARS.DIR.APPDATA, "profile.json");
        if (!file.exists()) {
            return false;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            JsonObject json = JsonObject.readFrom(reader);
            this.setMCUserName(json.get("mcUserName").asString());
            this.setClientToken(json.get("clientToken").asString());
            this.setAccessToken(json.get("accessToken").asString());
            this.setProfileID(json.get("profileID").asString());
            this.setLegacy(json.get("legacy").asBoolean());
            reader.close();
            return this.mcUserName != null && this.accessToken != null && this.clientToken != null && this.profileID != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
