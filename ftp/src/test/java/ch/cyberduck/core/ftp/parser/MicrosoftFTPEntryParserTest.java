package ch.cyberduck.core.ftp.parser;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.ftp.FTPParserSelector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MicrosoftFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("Windows_NT version 5.0");
    }

    @Test
    public void testParse() throws Exception {
        FTPFile parsed;

        // #3701
        parsed = parser.parseFTPEntry(
                "12-04-06  12:43PM                65335 fon1.kucuk.jpg"
        );
        assertNotNull(parsed);
        assertEquals("fon1.kucuk.jpg", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals(65335, parsed.getSize());
        assertEquals(2006, parsed.getTimestamp().get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(4, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }
}