package ch.cyberduck.ui.cocoa.logging;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.ui.cocoa.foundation.FoundationKitFunctionsLibrary;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Redirect to NSLog(). Logs an error message to the Apple System Log facility.
 *
 * @version $Id$
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
