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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.Strings;
import org.rococoa.internal.RococoaTypeMapper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Redirect to NSLog(). Logs an error message to the Apple System Log facility.
 */
public class SystemLogAppender extends AbstractAppender {

    private static final FoundationKitFunctions library = Native.load(
        "Foundation", FoundationKitFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    public SystemLogAppender(final Layout layout) {
        super(SystemLogAppender.class.getName(), null, layout, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(final LogEvent event) {
        if(null == event.getMessage()) {
            return;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(new String(getLayout().toByteArray(event), StandardCharsets.UTF_8));
        if(ignoreExceptions()) {
            final Throwable thrown = event.getThrown();
            if(thrown != null) {
                buffer.append(Strings.LINE_SEPARATOR);
                final String[] trace = ExceptionUtils.getStackFrames(thrown);
                for(final String t : trace) {
                    buffer.append(t).append(Strings.LINE_SEPARATOR);
                }
            }
        }
        library.NSLog("%@", buffer.toString());
    }
}
