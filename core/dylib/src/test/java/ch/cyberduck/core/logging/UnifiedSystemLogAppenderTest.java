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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Test;

public class UnifiedSystemLogAppenderTest {

    @Test
    public void testAppend() {
        final UnifiedSystemLogAppender a = new UnifiedSystemLogAppender();
        a.append(new Log4jLogEvent.Builder().setLoggerName(UnifiedSystemLogAppender.class.getCanonicalName()).setLevel(Level.DEBUG).setThrown(new RuntimeException()).setMessage(new SimpleMessage("Test")).build());
        a.append(new Log4jLogEvent.Builder().setLoggerName(UnifiedSystemLogAppender.class.getCanonicalName()).setLevel(Level.ERROR).setThrown(new RuntimeException()).setMessage(new SimpleMessage("Test")).build());
    }

    @Test
    public void testAppend유준환() {
        final UnifiedSystemLogAppender a = new UnifiedSystemLogAppender();
        a.log(UnifiedSystemLogAppender.OS_LOG_TYPE_INFO, "http-유준환.txt-1", "유준환");
    }
}
