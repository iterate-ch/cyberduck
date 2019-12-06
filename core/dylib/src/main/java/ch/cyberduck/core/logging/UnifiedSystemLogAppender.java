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

    /**
     * Default-level messages are initially stored in memory buffers. Without a configuration change, they are
     * compressed and moved to the data store as memory buffers fill. They remain there until a storage quota is
     * exceeded, at which point, the oldest messages are purged. Use this level to capture information about things that
     * might result a failure. Logging a message of this type is equivalent to calling the os_log function.
     */
    public static final int OS_LOG_TYPE_DEFAULT = 0x00;
    /**
     * Info-level messages are initially stored in memory buffers. Without a configuration change, they are not moved to
     * the data store and are purged as memory buffers fill. They are, however, captured in the data store when faults
     * and, optionally, errors occur. When info-level messages are added to the data store, they remain there until a
     * storage quota is exceeded, at which point, the oldest messages are purged. Use this level to capture information
     * that may be helpful, but isn’t essential, for troubleshooting errors. Logging a message of this type is
     * equivalent to calling the os_log_info function.
     */
    public static final int OS_LOG_TYPE_INFO = 0x01;
    /**
     * Debug-level messages are only captured in memory when debug logging is enabled through a configuration change.
     * They’re purged in accordance with the configuration’s persistence setting. Messages logged at this level contain
     * information that may be useful during development or while troubleshooting a specific problem. Debug logging is
     * intended for use in a development environment and not in shipping software. Logging a message of this type is
     * equivalent to calling the os_log_debug function.
     */
    public static final int OS_LOG_TYPE_DEBUG = 0x02;
    /**
     * Error-level messages are always saved in the data store. They remain there until a storage quota is exceeded, at
     * which point, the oldest messages are purged. Error-level messages are intended for reporting process-level
     * errors. If an activity object exists, logging at this level captures information for the entire process chain.
     * Logging a message of this type is equivalent to calling the os_log_error function.
     */
    public static final int OS_LOG_TYPE_ERROR = 0x10;
    /**
     * Fault-level messages are always saved in the data store. They remain there until a storage quota is exceeded, at
     * which point, the oldest messages are purged. Fault-level messages are intended for capturing system-level or
     * multi-process errors only. If an activity object exists, logging at this level captures information for the
     * entire process chain. Logging a message at this level is equivalent to calling the os_log_fault function.
     */
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
        // Category name
        final String logger = String.format("%s %s", event.getThreadName(), event.getLogger().getName());
        switch(event.getLevel().toInt()) {
            case Level.FATAL_INT:
            case Level.ERROR_INT:
                this.log(OS_LOG_TYPE_ERROR, logger, buffer.toString());
                break;
            case Level.TRACE_INT:
            case Level.DEBUG_INT:
                this.log(OS_LOG_TYPE_DEBUG, logger, buffer.toString());
                break;
            case Level.INFO_INT:
                this.log(OS_LOG_TYPE_INFO, logger, buffer.toString());
                break;
            case Level.WARN_INT:
            default:
                this.log(OS_LOG_TYPE_DEFAULT, logger, buffer.toString());
                break;
        }
    }

    public native void log(int type, String category, String message);

    @Override
    public void close() {
        //
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
