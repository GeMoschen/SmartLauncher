package de.gemo.smartlauncher.launcher.actions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.core.Main;
import de.gemo.smartlauncher.launcher.units.AuthData;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.GETResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPResponse;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.VARS;

public class LoginAction implements HTTPAction {

    private String userName, password;
    private String shortDescription;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;

    public LoginAction(String userName, String password) {
        this.shortDescription = "logging in...";
        this.userName = userName;
        this.password = password;
    }

    public HTTPResponse doAction() throws IOException {
        StatusFrame.INSTANCE.setText(this.getShortDescription());

        // some vars...
        this.contentLength = 1;
        this.loadedLength = 0;

        // POST ACTION
        URL url = new URL(VARS.URL.MinecraftLogin.GET_LOGIN);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        // add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", Worker.USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream writer = new DataOutputStream(con.getOutputStream());

        JsonObject json = new JsonObject();
        JsonObject agentObject = new JsonObject();
        agentObject.add("name", "Minecraft");
        agentObject.add("version", 1);
        json.add("agent", agentObject);
        json.add("username", this.userName);
        json.add("password", this.password);

        AuthData loginData = Main.authData;
        loginData.resetData();
        this.userName = null;
        this.password = null;

        writer.writeBytes(json.toString());
        writer.flush();
        writer.close();

        this.responseCode = con.getResponseCode();
        BufferedReader in;
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        this.loadedLength = this.contentLength;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new GETResponse("LoginOK", responseCode, this.getContentLength(), con.getContentType(), response);
        }
        // return
        loginData.resetData();
        loginData.save();
        return new GETResponse("Login failed", responseCode, this.getContentLength(), con.getContentType(), response);
    }

    public long getLoadedLength() {
        return loadedLength;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getCompleteURL() {
        return VARS.URL.MinecraftLogin.GET_LOGIN;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
