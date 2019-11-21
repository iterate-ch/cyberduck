package ch.cyberduck.core.logging;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.library.Native;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Redirect to NSLog(). Logs an error message to the Apple System Log facility.
 */
public class UnifiedSystemLogAppender extends AppenderSkeleton {

    static {
        Native.load("core");
    }

    public static final int OS_LOG_TYPE_DEFAULT = 0x00;
    public static final int OS_LOG_TYPE_INFO = 0x01;
    public static final int OS_LOG_TYPE_DEBUG = 0x02;
    public static final int OS_LOG_TYPE_ERROR = 0x10;
    public static final int OS_LOG_TYPE_FAULT = 0x11;

    @Override
    protected void append(final LoggingEvent event) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(event.getMessage());
        if(layout.ignoresThrowable()) {
            final String[] trace = event.getThrowableStrRep();
            if(trace != null) {
                buffer.append(Layout.LINE_SEP);
                for(final String t : trace) {
                    buffer.append(t).append(Layout.LINE_SEP);
                }
            }
        }
        switch(event.getLevel().toInt()) {
            case Level.ERROR_INT:
                this.log(OS_LOG_TYPE_ERROR, buffer.toString());
                break;
            case Level.WARN_INT:
                this.log(OS_LOG_TYPE_FAULT, buffer.toString());
                break;
            case Level.INFO_INT:
                this.log(OS_LOG_TYPE_INFO, buffer.toString());
                break;
            default:
                this.log(OS_LOG_TYPE_DEBUG, buffer.toString());
                break;
        }
    }

    private native void log(int type, String message);

    @Override
    public void close() {
        //
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
