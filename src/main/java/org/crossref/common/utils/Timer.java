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

    /**
     * Start the timer.
     */
    public void start() {
        startTime = Calendar.getInstance();
        stopTime = startTime;
    }

    /**
     * Stop the timer.
     */
    public void stop() {
        stopTime = Calendar.getInstance();
    }

    /**
     * Get Elapsed time as a computed string of the form "N mins, N secs, N ms"
     * @return A formatted string
     */
    public String elapsedTime() {
        long millis = elapsedMs();
        long secs = millis / 1000;

        millis -= (secs * 1000);

        long mins = secs / 60;

        secs -= (mins * 60);

        return String.format("%02d mins, %02d secs, %03d ms", mins, secs, millis);
    } 
    
    /**
     * Get elapsed time in milliseconds.
     * @return Number of milliseconds
     */
    public long elapsedMs() {
        return stopTime.getTimeInMillis() - startTime.getTimeInMillis();
    }
}
