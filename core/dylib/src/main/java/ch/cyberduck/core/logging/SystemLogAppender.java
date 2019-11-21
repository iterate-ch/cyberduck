package ch.cyberduck.core.logging;

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

import ch.cyberduck.binding.foundation.FoundationKitFunctions;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Redirect to NSLog(). Logs an error message to the Apple System Log facility.
 */
public class SystemLogAppender extends AppenderSkeleton {

    private static final FoundationKitFunctions library = Native.load(
        "Foundation", FoundationKitFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    @Override
    protected void append(final LoggingEvent event) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(layout.format(event));
        if(layout.ignoresThrowable()) {
            final String[] trace = event.getThrowableStrRep();
            if(trace != null) {
                buffer.append(Layout.LINE_SEP);
                for(final String t : trace) {
                    buffer.append(t).append(Layout.LINE_SEP);
                }
            }
        }
        library.NSLog("%@", buffer.toString());
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
