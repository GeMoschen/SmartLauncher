package de.gemo.smartlauncher.internet;

public class DownloadResponse implements HTTPResponse {

    private final int status;
    private final String url;
    private final int responseCode;
    private final long contentLength;

    public DownloadResponse(int status, String url, int responseCode, long contentLength) {
        this.status = status;
        this.url = url;
        this.responseCode = responseCode;
        this.contentLength = contentLength;
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
