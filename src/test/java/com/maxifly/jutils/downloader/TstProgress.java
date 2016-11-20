package com.maxifly.jutils.downloader;

import com.maxifly.jutils.I_Progress;

/**
 * Created by Maximus on 19.11.2016.
 */
public class TstProgress implements I_Progress {
    public long max = 0;
    public long done = 0;
    public String mess = null;

    @Override
    public void setMaxValue(long max) {
        this.max = max;

    }

    @Override
    public void incrementDone(long increment) {
        done = done + increment;

    }

    @Override
    public void incrementDone(long increment, String message) {
        done = done + increment;
        mess = message;
    }

    @Override
    public void updateProgress(long workDone, long max) {
        this.done = workDone;
        this.max = max;

    }

    @Override
    public void updateProgress(long workDone, long max, String message) {
        this.done = workDone;
        this.max = max;
        mess = message;
    }
}
