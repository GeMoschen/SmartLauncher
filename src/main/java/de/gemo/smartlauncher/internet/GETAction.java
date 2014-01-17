package de.gemo.smartlauncher.internet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GETAction implements HTTPAction {

    private final String fileURL;
    private String completeURL;
    private String shortDescription;
    private int responseCode = 0;
    private final ArrayList<GETProperty> properties;

    private long contentLength = 0;
    private long loadedLength = 0;

    public GETAction(String shortDescription, String fileURL) {
        this(shortDescription, fileURL, null);
    }

    public GETAction(String shortDescription, String fileURL, ArrayList<GETProperty> properties) {
        this.shortDescription = shortDescription;
        this.fileURL = fileURL;
        this.properties = properties;
        this.buildCompleteURL();
    }

    private void buildCompleteURL() {
        this.completeURL = fileURL;
        // append GET-Values
        if (this.properties != null && this.properties.size() > 0) {
            this.completeURL += '?';
            GETProperty property;
            for (int index = 1; index <= properties.size(); index++) {
                property = properties.get(index - 1);
                this.completeURL += property.toString();
                if (index != properties.size()) {
                    this.completeURL += '&';
                }
            }
        }
    }

    public HTTPResponse doAction() throws IOException {
        // create URL
        URL url = new URL(this.completeURL);
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
        BufferedReader input;
        String inputLine;
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            input = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            input = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        while ((inputLine = input.readLine()) != null) {
            response.append(inputLine);
            this.loadedLength += inputLine.length();
        }
        input.close();
        this.loadedLength = this.contentLength;

        // return
        return new GETResponse(this.completeURL, responseCode, this.getContentLength(), con.getContentType(), response);
    }

    public long getLoadedLength() {
        return loadedLength;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getCompleteURL() {
        return completeURL;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    @Override
    public int getResponseCode() {
        return this.responseCode;
    }
}
