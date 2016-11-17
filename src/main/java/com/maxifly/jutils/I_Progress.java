package com.maxifly.jutils;

/**
 * Created by Maximus on 06.11.2016.
 */
public interface I_Progress {
    void setMaxValue(long max);

    void incrementDone(long increment);

    void incrementDone(long increment, String message);

    void updateProgress(long workDone, long max);

    void updateProgress(long workDone, long max, String message);
}
