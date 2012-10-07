package ch.cyberduck.ui.cocoa.logging;

import ch.cyberduck.ui.cocoa.foundation.FoundationKitFunctionsLibrary;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Redirect to NSLog(). Logs an error message to the Apple System Log facility.
 *
 * @version $Id:$
 */
public class SystemLogAppender extends AppenderSkeleton {

    @Override
    protected void append(final LoggingEvent event) {
        FoundationKitFunctionsLibrary.NSLog(layout.format(event));
    }

    @Override
    public synchronized void doAppend(final LoggingEvent event) {
        if(event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            // Restrict to error level
            super.doAppend(event);
        }
    }

    @Override
    public void close() {
        //
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
