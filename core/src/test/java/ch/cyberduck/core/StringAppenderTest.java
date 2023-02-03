package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringAppenderTest {

    @Test
    public void testAppend() {
        assertEquals("Verification Code.", new StringAppender().append("Verification Code:").toString());
        assertEquals("Message.", new StringAppender().append("Message").toString());
        assertEquals("Message.", new StringAppender().append("Message.").toString());
        assertEquals("Message? t.", new StringAppender().append("Message?").append("t").toString());
        assertEquals("Message).", new StringAppender().append("Message)").toString());
        assertEquals("m.", new StringAppender().append("m").append(" ").toString());
    }

    @Test
    public void testAppendNewLine() {
        assertEquals("", new StringAppender().append("\n").toString());
        assertEquals("t.", new StringAppender().append("t").append("\n").toString());
    }

    @Test
    public void testAppendControlChar() {
        assertEquals("", new StringAppender().append("\u000F").toString());
        assertEquals("t.", new StringAppender().append("t").append("\u000F").toString());
    }

    @Test
    public void testAppendEmpty() {
        assertEquals("", new StringAppender().append("").toString());
        assertEquals("t.", new StringAppender().append("t").append("").toString());
    }

    @Test
    public void testAppendNull() {
        assertEquals("", new StringAppender().append(null).toString());
        assertEquals("t.", new StringAppender().append("t").append(null).toString());
    }
}
