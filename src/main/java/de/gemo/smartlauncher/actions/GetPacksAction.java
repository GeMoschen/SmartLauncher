package de.gemo.smartlauncher.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.GETResponse;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPResponse;
import de.gemo.smartlauncher.internet.Worker;

public class GetPacksAction implements HTTPAction {

    private final String URL = "http://www.djgemo.de/packs.json";
    private String shortDescription;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;

    public GetPacksAction() {
        this.shortDescription = "getting packs...";
    }

    public HTTPResponse doAction() throws IOException {
        StatusFrame.INSTANCE.setText(this.getShortDescription());

        // create URL
        URL url = new URL(URL);
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
        return new GETResponse(URL, responseCode, this.getContentLength(), con.getContentType(), response);
    }

    public long getLoadedLength() {
        return loadedLength;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getCompleteURL() {
        return this.URL;
    }

    @Override
    public int getResponseCode() {
        return this.responseCode;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }
}
