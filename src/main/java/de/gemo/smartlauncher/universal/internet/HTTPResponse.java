package de.gemo.smartlauncher.universal.internet;

public interface HTTPResponse {

    public abstract long getContentLength();

    public abstract String getContentType();

    public abstract int getResponseCode();
}
