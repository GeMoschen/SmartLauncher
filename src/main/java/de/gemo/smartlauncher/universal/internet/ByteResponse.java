package de.gemo.smartlauncher.universal.internet;

public class ByteResponse implements HTTPResponse {

    private final String url;
    private final int responseCode;
    private final long contentLength;
    private final String contentType;
    private final byte[] response;

    public ByteResponse(String url, int responseCode, long contentLength, String contentType, byte[] response) {
        this.url = url;
        this.responseCode = responseCode;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.response = response;
    }

    public String getURL() {
        return url;
    }

    public byte[] getResponse() {
        return response;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String toString() {
        String text = "-------------------------------------\n";
        text += "CLASS : " + this.getClass().getSimpleName() + ".class" + "\n";
        text += "URL: " + this.getURL() + "\n";
        text += "CODE: " + this.getResponseCode() + "\n";
        text += "LENGTH: " + this.getContentLength() + "\n";
        text += "TYPE: " + this.getContentType() + "\n";
        text += "-------------------------------------";
        return text;
    }
}
