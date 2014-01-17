package de.gemo.smartlauncher.internet;

public abstract class HTTPListener {

    private Worker worker;

    public final synchronized void setWorker(Worker worker) {
        this.worker = worker;
    }

    public final synchronized Worker getWorker() {
        return this.worker;
    }

    public abstract void onProgress(int maximumLength, int currentLength);

    public abstract void onStart(HTTPAction action);

    public abstract void onFinish(HTTPAction action);

    public abstract void onError(HTTPAction action);

}
