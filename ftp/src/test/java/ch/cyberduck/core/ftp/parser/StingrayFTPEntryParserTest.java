package ch.cyberduck.core.ftp.parser;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.ftp.FTPParserSelector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class StingrayFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("MACOS");
    }

    /**
     * http://trac.cyberduck.ch/ticket/1198
     */
    @Test
    public void testFile() {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "-r--r--r--          0     165100     165100 Aug  1 10:24 grau2.tif"
        );
        assertNotNull(parsed);
        assertEquals("grau2.tif", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals(Calendar.AUGUST, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(1, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(10, parsed.getTimestamp().get(Calendar.HOUR_OF_DAY));
        assertEquals(24, parsed.getTimestamp().get(Calendar.MINUTE));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    /**
     * http://trac.cyberduck.ch/ticket/1198
     */
    @Test
    public void testFolder() {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "dr--r--r--                folder          0 Aug  1 10:18 TestCyberduck"
        );
        assertNotNull(parsed);
        assertEquals("TestCyberduck", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals(Calendar.AUGUST, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(1, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(10, parsed.getTimestamp().get(Calendar.HOUR_OF_DAY));
        assertEquals(18, parsed.getTimestamp().get(Calendar.MINUTE));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }
}
