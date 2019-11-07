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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

public class UnifiedSystemLogAppenderTest {

    @Test
    public void testAppend() {
        final UnifiedSystemLogAppender a = new UnifiedSystemLogAppender();
        a.setLayout(new SimpleLayout());
        a.append(new LoggingEvent("f", Logger.getLogger(UnifiedSystemLogAppender.class),
            Level.DEBUG, "Test", new RuntimeException()));
        a.append(new LoggingEvent("f", Logger.getLogger(UnifiedSystemLogAppender.class),
            Level.ERROR, "Test", new RuntimeException()));
    }
}
