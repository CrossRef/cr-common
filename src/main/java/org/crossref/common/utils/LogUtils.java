package org.crossref.common.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Defines utility calls to support logging.
 * 
 * @author joe.aparo
 */
public final class LogUtils {
    /**
     * Get a logger for the top class in the thread stack.
     * 
     * @return A logger
     S*/
    public static Logger getLogger() {
        final Throwable t = new Throwable();
        t.fillInStackTrace();
        final String clazz = t.getStackTrace()[1].getClassName();
        return getLogger(clazz);
    }
    
    /**
     * Get a named logger.
     * @param name Logger name
     * 
     * @return A logger
     */
    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }
}
