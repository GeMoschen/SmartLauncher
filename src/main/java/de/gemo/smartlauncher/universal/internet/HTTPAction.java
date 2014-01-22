package de.gemo.smartlauncher.universal.internet;

import java.io.IOException;

public interface HTTPAction {

    public abstract HTTPResponse doAction() throws IOException;

    public abstract long getLoadedLength();

    public abstract long getContentLength();

    public abstract String getCompleteURL();

    public abstract String getShortDescription();

    public abstract int getResponseCode();
}
