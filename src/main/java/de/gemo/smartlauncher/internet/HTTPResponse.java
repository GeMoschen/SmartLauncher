package de.gemo.smartlauncher.internet;

public interface HTTPResponse {

    public abstract long getContentLength();

    public abstract String getContentType();

    public abstract int getResponseCode();
}
