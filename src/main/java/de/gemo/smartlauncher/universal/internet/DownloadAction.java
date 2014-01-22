package de.gemo.smartlauncher.universal.internet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAction implements HTTPAction, Listenerable {

    private final String fileURL, saveDir, fileName;

    private long contentLength = 0;
    private long loadedLength = 0;
    private int responseCode;
    private HTTPListener listener;

    public DownloadAction(String fileURL, String saveDir, String fileName) {
        this.fileURL = fileURL;
        this.saveDir = saveDir;
        this.fileName = fileName;
    }

    public HTTPResponse doAction() throws IOException {
        File dir = new File(saveDir);
        dir.mkdirs();

        URL url = new URL(fileURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // optional, default is GET
        con.setRequestMethod("GET");

        // add user-agent
        con.setRequestProperty("User-Agent", Worker.USER_AGENT);

        // always check HTTP response code first
        this.responseCode = con.getResponseCode();
        if (this.responseCode == HttpURLConnection.HTTP_OK) {
            this.contentLength = con.getContentLength();
            this.loadedLength = 0;

            // opens input stream from the HTTP connection
            InputStream inputStream = con.getInputStream();
            String tempFilePath = saveDir + File.separator + fileName + ".tmp";

            // delete tempfile...
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                tempFile.delete();
            }

            // skip file, if the file exists (and the size is equal)...
            String saveFilePath = saveDir + File.separator + fileName;
            File newFile = new File(saveFilePath);
            if (newFile.exists() && newFile.length() == this.contentLength) {
                return new DownloadResponse(1, this.fileURL, this.saveDir, this.fileName, responseCode, contentLength);
            }

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(tempFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[Worker.BUFFER_SIZE];
            long lastUpdate = System.currentTimeMillis();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                this.loadedLength += bytesRead;
                if (lastUpdate + 100 < System.currentTimeMillis()) {
                    lastUpdate = System.currentTimeMillis();
                    if (this.listener != null) {
                        this.listener.onProgress((int) this.contentLength, (int) this.loadedLength);
                    }
                }
            }

            outputStream.close();
            inputStream.close();

            // rename file...
            if (newFile.exists()) {
                newFile.delete();
            }
            tempFile.renameTo(newFile);

            con.disconnect();
            return new DownloadResponse(1, this.fileURL, this.saveDir, this.fileName, responseCode, contentLength);
        } else {
            con.disconnect();
            return new DownloadResponse(-1, this.fileURL, this.saveDir, this.fileName, responseCode, 0);
        }
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLoadedLength() {
        return loadedLength;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getCompleteURL() {
        return this.fileURL;
    }

    public String getShortDescription() {
        return this.fileName;
    }

    @Override
    public int getResponseCode() {
        return this.responseCode;
    }

    public void setListener(HTTPListener listener) {
        this.listener = listener;
    }

    @Override
    public HTTPListener getListener() {
        return this.listener;
    }

}
