package com.maxifly.jutils.downloader;

import com.maxifly.jutils.I_Progress;
import com.maxifly.jutils.downloader.Download;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Maximus on 02.06.2016.
 */
public class Downloader
implements AutoCloseable
{
    private ExecutorService es;
    private Map<Download, Future<DownStatus>> tasks = new HashMap();
    private I_Progress progress = null;
    private int complete_task = 0;
    private int all_tasks = 0;

    public Downloader() {
        es = Executors.newFixedThreadPool(5);
    }

    public void setProgressMonitor(I_Progress progress) {
        this.progress = progress;
    }

    public void startTask(Download download) {
        if (es == null) {
            es = Executors.newFixedThreadPool(5);
        }

        Future<DownStatus> future = es.submit(download);
        tasks.put(download, future);
        all_tasks++;
        change_progress();
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
                    complete_task++;
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

        change_progress();

        // Теперь рестартуем
        for (Download download : restart) {
            this.startTask(download);
        }

        System.out.println("steelExecute " + steelExecute);
        return !steelExecute;

    }

    @Override
    public void close() throws Exception {
        try {
            System.out.println("attempt to shutdown executor");
            es.shutdown();
            es.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!es.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            es.shutdownNow();
            System.out.println("shutdown finished");
        }
        es = null;
    }

    private void change_progress() {
        if (progress != null) {
           progress.updateProgress(complete_task,all_tasks,"Загружено " + complete_task + " из " + all_tasks);
        }
    }

}
