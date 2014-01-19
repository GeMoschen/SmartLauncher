package de.gemo.smartlauncher.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.ByteResponse;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.internet.HTTPResponse;
import de.gemo.smartlauncher.internet.Listenerable;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.units.Pack;
import de.gemo.smartlauncher.units.VARS;

public class GetSinglePackAction implements HTTPAction, Listenerable {

    private String shortDescription;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;
    private String packURL;
    private final Pack pack;
    private final String packVersion;
    private HTTPListener listener;

    public GetSinglePackAction(Pack pack, String packVersion) {
        this.pack = pack;
        this.packVersion = packVersion;
        this.shortDescription = "getting pack '" + this.pack.getPackName() + " - " + this.packVersion + "'...";
        this.packURL = (VARS.URL.PACKSERVER + "packs/" + this.pack.getPackName() + "/" + this.pack.getPackName() + "-" + this.packVersion + ".zip");
    }

    public HTTPResponse doAction() throws IOException {
        // set info...
        StatusFrame.INSTANCE.setText(this.getShortDescription());

        // create URL...
        URL url = new URL(this.packURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // optional, default is GET...
        con.setRequestMethod("GET");

        // add user-agent...
        con.setRequestProperty("User-Agent", Worker.USER_AGENT);

        // get responseCode
        this.responseCode = con.getResponseCode();
        this.contentLength = con.getContentLength();
        this.loadedLength = 0;

        // read response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new ByteResponse(this.packURL, responseCode, this.getContentLength(), con.getContentType(), getBytes(con.getInputStream()));
        } else {
            StringBuffer response = new StringBuffer();
            BufferedReader input;
            String inputLine;
            input = new BufferedReader(new InputStreamReader(con.getErrorStream())); // append
                                                                                     // response
            while ((inputLine = input.readLine()) != null) {
                response.append(inputLine);
                this.loadedLength += inputLine.length();
            }

            // close
            input.close();
            this.loadedLength = this.contentLength;

            // returns
            return new ByteResponse(this.packURL, responseCode, this.getContentLength(), con.getContentType(), response.toString().getBytes());
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            long lastUpdate = System.currentTimeMillis();
            while ((len = is.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, len);
                this.loadedLength += len;
                if (lastUpdate + 100 < System.currentTimeMillis()) {
                    lastUpdate = System.currentTimeMillis();
                    if (this.listener != null) {
                        this.listener.onProgress((int) this.contentLength, (int) this.loadedLength);
                    }
                }
            }
            buf = bos.toByteArray();
        }
        return buf;
    }

    public long getLoadedLength() {
        return loadedLength;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getCompleteURL() {
        return this.packURL;
    }

    @Override
    public int getResponseCode() {
        return this.responseCode;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public Pack getPack() {
        return pack;
    }

    public String getPackVersion() {
        return packVersion;
    }

    @Override
    public void setListener(HTTPListener listener) {
        this.listener = listener;
    }

    @Override
    public HTTPListener getListener() {
        return this.listener;
    }
}
