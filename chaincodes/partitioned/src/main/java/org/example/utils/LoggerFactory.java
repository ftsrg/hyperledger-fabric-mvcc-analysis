package org.example.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class LoggerFactory {
    public static volatile ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>();

    public static synchronized Logger getLogger(final String classname) {
        Logger logger = loggers.get(classname);
        if (logger == null) {
            logger = Logger.getLogger(classname);
            logger.setUseParentHandlers(false);
            final LogFormatter fmt = new LogFormatter();
            final ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(fmt);
            logger.addHandler(handler);
            loggers.put(classname, logger);
        }
        return logger;
    }
}
