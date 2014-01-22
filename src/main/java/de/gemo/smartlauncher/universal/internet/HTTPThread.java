package de.gemo.smartlauncher.universal.internet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gemo.smartlauncher.universal.units.Logger;

public class HTTPThread implements Runnable {

    private final List<Worker> workerList;

    public HTTPThread() {
        this.workerList = Collections.synchronizedList(new ArrayList<Worker>());
    }

    public void appendWorker(Worker worker) {
        this.workerList.add(worker);
    }

    public void run() {
        // iterate over every worker...
        Worker worker;
        for (int index = 0; index < workerList.size(); index++) {
            worker = this.workerList.get(index);
            worker.start();
            while (!worker.isFinished()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (worker.isError()) {
                Logger.error("Could not connect to '" + worker.getAction().getCompleteURL() + "'!");
                continue;
            }
        }
        clear();
    }

    public void clear() {
        this.workerList.clear();
    }

}
