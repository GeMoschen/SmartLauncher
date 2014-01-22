package de.gemo.smartlauncher.universal.internet;

public class Worker implements Runnable {

    public static final int BUFFER_SIZE = 4096;
    public static final String USER_AGENT = "Mozilla/5.0";

    private final HTTPAction action;
    private HTTPResponse response = null;
    private HTTPListener listener = null;
    private boolean started = false, finished = false, error = false;;

    public Worker(HTTPAction action) {
        this(action, null);
    }

    public Worker(HTTPAction action, HTTPListener listener) {
        this.action = action;
        this.listener = listener;
    }

    public synchronized void start() {
        if (this.action instanceof Listenerable) {
            ((Listenerable) this.action).setListener(listener);
        }
        if (this.listener != null) {
            this.listener.setWorker(this);
        }
        new Thread(this).start();
    }

    public void run() {
        try {
            this.started = true;
            if (this.listener != null) {
                this.listener.onStart(this.action);
            }
            this.response = this.action.doAction();
            if (this.listener != null) {
                this.listener.onFinish(this.action);
            }
            this.finished = true;
            this.error = false;
        } catch (Exception e) {
            e.printStackTrace();
            this.error = true;
            this.finished = true;
            if (this.listener != null) {
                this.listener.onError(this.action);
            }
        }
    }

    public boolean isError() {
        return error;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isStarted() {
        return started;
    }

    public HTTPResponse getResponse() {
        return response;
    }

    public HTTPAction getAction() {
        return action;
    }

    public HTTPListener getListener() {
        return listener;
    }
}
