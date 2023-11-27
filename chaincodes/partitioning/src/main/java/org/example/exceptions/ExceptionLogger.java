package org.example.exceptions;

import java.util.Arrays;
import java.util.logging.Logger;

public class ExceptionLogger {

    public static void log(Logger log, Exception e) {
        log.severe(e.getClass().getName() + " " + e.getMessage());
        Throwable cause = e.getCause();
        while (cause != null) {
            log.severe("caused by:");
            log.severe(cause.toString());
            cause = cause.getCause();
        }

        Arrays.stream(e.getStackTrace()).forEach(trace -> {
            log.severe(trace.toString());
        });
    }
}
