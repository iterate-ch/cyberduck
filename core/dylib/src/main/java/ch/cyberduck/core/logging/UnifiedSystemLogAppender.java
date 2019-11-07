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

import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import static ch.cyberduck.binding.foundation.FoundationKitFunctionsLibrary.*;

/**
 * Redirect to NSLog(). Logs an error message to the Apple System Log facility.
 */
public class UnifiedSystemLogAppender extends AppenderSkeleton {

    private final NSObject category = os_log_create(
        PreferencesFactory.get().getProperty("application.identifier"), PreferencesFactory.get().getProperty("application.name"));

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
                os_log_with_type(category, OS_LOG_TYPE_ERROR, "%@", buffer.toString());
                break;
            case Level.WARN_INT:
                os_log_with_type(category, OS_LOG_TYPE_FAULT, "%@", buffer.toString());
                break;
            case Level.INFO_INT:
                os_log_with_type(category, OS_LOG_TYPE_INFO, "%@", buffer.toString());
                break;
            default:
                os_log_with_type(category, OS_LOG_TYPE_DEBUG, "%@", buffer.toString());
                break;
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
