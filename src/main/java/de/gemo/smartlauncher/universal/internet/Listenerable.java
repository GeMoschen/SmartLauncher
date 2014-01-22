package de.gemo.smartlauncher.universal.internet;

public interface Listenerable {

    public abstract void setListener(HTTPListener listener);

    public abstract HTTPListener getListener();
}
