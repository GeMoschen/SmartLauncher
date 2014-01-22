package de.gemo.smartlauncher.universal.internet;

public class DownloadResponse implements HTTPResponse {

    private final int status;
    private final String url;
    private final int responseCode;
    private final long contentLength;
    private final String dir, fileName;

    public DownloadResponse(int status, String url, String dir, String fileName, int responseCode, long contentLength) {
        this.status = status;
        this.url = url;
        this.responseCode = responseCode;
        this.contentLength = contentLength;
        this.dir = dir;
        this.fileName = fileName;
    }

    public String getURL() {
        return url;
    }

    public long getContentLength() {
        return contentLength;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getDir() {
        return dir;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        String text = "-------------------------------------\n";
        text += "CLASS : " + this.getClass().getSimpleName() + ".class" + "\n";
        text += "STATUS: " + this.getStatus() + "\n";
        text += "URL: " + this.getURL() + "\n";
        text += "CODE: " + this.getResponseCode() + "\n";
        text += "LENGTH: " + this.getContentLength() + "\n";
        text += "-------------------------------------";
        return text;
    }

    public int getStatus() {
        return this.status;
    }

    public String getContentType() {
        return "application/json";
    }
}
