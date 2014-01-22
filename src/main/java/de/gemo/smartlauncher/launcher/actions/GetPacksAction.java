package de.gemo.smartlauncher.launcher.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.GETResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPResponse;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.VARS;

public class GetPacksAction implements HTTPAction {

    private String shortDescription;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;
    private String packsURL;

    public GetPacksAction() {
        this.shortDescription = "getting packs...";
        this.packsURL = (VARS.URL.PACKSERVER + "packs.json");
        // this.packsURL = (VARS.URL.JSON.PACKSERVER + "packs.php?userName=" +
        // Main.authData.getMCUserName());
    }

    public HTTPResponse doAction() throws IOException {
        StatusFrame.INSTANCE.setText(this.getShortDescription());
        // create URL

        URL url = new URL(this.packsURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // optional, default is GET
        con.setRequestMethod("GET");

        // add user-agent
        con.setRequestProperty("User-Agent", Worker.USER_AGENT);

        // get responseCode
        this.responseCode = con.getResponseCode();
        this.contentLength = con.getContentLength();
        this.loadedLength = 0;

        // read response
        StringBuffer response = new StringBuffer();
        BufferedReader input;
        String inputLine;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            input = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            input = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        // append response
        while ((inputLine = input.readLine()) != null) {
            response.append(inputLine);
            this.loadedLength += inputLine.length();
        }
        // close
        input.close();
        this.loadedLength = this.contentLength;

        // return
        return new GETResponse(this.packsURL, responseCode, this.getContentLength(), con.getContentType(), response);
    }

    public long getLoadedLength() {
        return loadedLength;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getCompleteURL() {
        return this.packsURL;
    }

    @Override
    public int getResponseCode() {
        return this.responseCode;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

}
