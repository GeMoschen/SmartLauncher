package de.gemo.smartlauncher.internet;

public interface Listenerable {

    public abstract void setListener(HTTPListener listener);

    public abstract HTTPListener getListener();
}
