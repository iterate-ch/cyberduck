package ch.cyberduck.core.threading;

import org.apache.log4j.Logger;

public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger log = Logger.getLogger(LoggingUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        // Swallow the exception
        log.error(String.format("Thread %s has thrown uncaught exception:%s",
                t.getName(), e.getMessage()), e);
    }
}
