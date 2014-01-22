package de.gemo.smartlauncher.launcher.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.ByteResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPResponse;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.VARS;

public class GetPackIconAction implements HTTPAction {

    private String shortDescription;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;

    private Pack pack;

    public GetPackIconAction(Pack pack) {
        this.shortDescription = "getting icon for '" + pack.getPackName() + "'...";
        this.pack = pack;
    }

    public HTTPResponse doAction() throws IOException {
        StatusFrame.INSTANCE.setText(this.getShortDescription());

        // create URL
        String URL = (VARS.URL.PACKSERVER + "packs/" + this.pack.getPackName() + "/icon.png");
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
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new ByteResponse(URL, responseCode, this.getContentLength(), con.getContentType(), getBytes(con.getInputStream()));
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
            return new ByteResponse(URL, responseCode, this.getContentLength(), con.getContentType(), response.toString().getBytes());
        }

    }

    public static byte[] getBytes(InputStream is) throws IOException {
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
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
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
        return VARS.URL.MinecraftLogin.GET_LOGIN;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public Pack getPack() {
        return pack;
    }

    @Override
    public int getResponseCode() {
        return this.responseCode;
    }
}
