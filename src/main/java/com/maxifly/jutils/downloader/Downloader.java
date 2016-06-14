package com.maxifly.jutils.downloader;

import com.maxifly.jutils.downloader.Download;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Maximus on 02.06.2016.
 */
public class Downloader {
    private ExecutorService es;
    private Map<Download, Future<DownStatus>> tasks = new HashMap();

    public Downloader() {
        es = Executors.newFixedThreadPool(5);
    }

    public void startTask(Download download) {
        Future<DownStatus> future = es.submit(download);
        tasks.put(download, future);
    }

    public boolean checkTasks() throws ExecutionException, InterruptedException {
        boolean steelExecute = false;

        Set<Download> restart = new HashSet<>();
        for (Map.Entry<Download, Future<DownStatus>> entry : tasks.entrySet()) {
            Future<DownStatus> future = entry.getValue();
            Download download = entry.getKey();
            if (future.isDone()) {
                if (future.get() == DownStatus.COMPLITE) {
                    System.out.println("Download " + download.getUrl().getPath() + " complete");
                } else {
                    System.out.println("Download " + download.getUrl().getPath() + " not complete");
                    restart.add(download);
                    steelExecute = true;
                }
            } else if (future.isCancelled()) {
                System.out.println("Download " + download.getUrl().getPath() + " cancelled");
                restart.add(download);
                steelExecute = true;
            } else {
                // Еще выполняется
                steelExecute = true;
            }
        }

        // Теперь рестартуем
        for (Download download : restart) {
            this.startTask(download);
        }

        System.out.println("steelExecute " + steelExecute);
        return !steelExecute;

    }

}
