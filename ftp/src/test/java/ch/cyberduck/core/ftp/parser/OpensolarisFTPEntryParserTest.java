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

import static org.junit.Assert.*;

public class OpensolarisFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("opensolaris FTP server ready.");
    }

    @Test
    public void testParse() throws Exception {
        FTPFile parsed;

        // #3689
        parsed = parser.parseFTPEntry(
                "drwxr-xr-x+  5 niels    staff          7 Sep  6 13:46 data"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "data");
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals(7, parsed.getSize());
        assertEquals(Calendar.SEPTEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(6, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }
}