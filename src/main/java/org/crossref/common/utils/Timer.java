package org.crossref.common.utils;

import java.util.Calendar;

/**
 * Simple stopwatch implementation.
 * 
 * @author joe.aparo
 */
public class Timer {
    private Calendar startTime;
    private Calendar stopTime;

    public void start() {
        startTime = Calendar.getInstance();
        stopTime = startTime;
    }

    public void stop() {
        stopTime = Calendar.getInstance();
    }

    public String elapsedTime() {
        long millis = stopTime.getTimeInMillis() - startTime.getTimeInMillis();
        long secs = millis / 1000;

        millis -= (secs * 1000);

        long mins = secs / 60;

        secs -= (mins * 60);

        return String.format("%02d mins, %02d secs, %03d ms", mins, secs, millis);
    } 
}
