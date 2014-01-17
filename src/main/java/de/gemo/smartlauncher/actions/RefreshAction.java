package de.gemo.smartlauncher.actions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.core.Main;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.GETResponse;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPResponse;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.units.AuthData;
import de.gemo.smartlauncher.units.VARS;

public class RefreshAction implements HTTPAction {

    private AuthData loginData;
    private String shortDescription;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;

    public RefreshAction(AuthData loginData) {
        this.shortDescription = "logging in...";
        this.loginData = loginData;
    }

    public HTTPResponse doAction() throws IOException {
        StatusFrame.INSTANCE.setText(this.getShortDescription());

        // some vars...
        this.contentLength = 1;
        this.loadedLength = 0;

        // POST ACTION
        URL url = new URL(VARS.URL.MinecraftLogin.REFRESH_LOGIN);
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
        json.add("accessToken", this.loginData.getAccessToken());
        json.add("clientToken", this.loginData.getClientToken());

        // NOT USED FOR NOW
        // JsonObject profile = new JsonObject();
        // profile.add("id", this.profileID); profile.add("name",
        // this.userName); json.add("selectedProfile", profile);

        AuthData loginData = Main.authData;
        loginData.resetData();

        writer.writeBytes(json.toString());
        writer.flush();
        writer.close();

        responseCode = con.getResponseCode();

        StringBuffer response = new StringBuffer();
        BufferedReader input;
        String inputLine;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            input = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            input = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        while ((inputLine = input.readLine()) != null) {
            response.append(inputLine);
        }
        input.close();
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
        return VARS.URL.MinecraftLogin.REFRESH_LOGIN;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
