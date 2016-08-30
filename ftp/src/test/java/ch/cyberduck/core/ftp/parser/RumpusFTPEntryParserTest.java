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

public class RumpusFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("MACOS");
    }

    @Test
    public void testParse() throws Exception {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwxr-xr-x               folder        0 Oct 18 13:02 Akrilik"
        );
        assertNotNull(parsed);
        assertEquals("Akrilik", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(18, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        parsed = parser.parseFTPEntry(
                "drwxrwxrwx               folder        0 Oct 11 14:53 Uploads"
        );
        assertNotNull(parsed);
        assertEquals("Uploads", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(11, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        parsed = parser.parseFTPEntry(
                "-rw-r--r--        0      589878   589878 Oct 15 13:03 WebDAV SS.bmp"
        );
        assertNotNull(parsed);
        assertEquals("WebDAV SS.bmp", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(15, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Test
    public void testUnknownSystIdentifier() throws Exception {
        this.parser = new FTPParserSelector().getParser("Digital Domain FTP");

        FTPFile parsed;
        parsed = parser.parseFTPEntry(
                "drwxrwxrwx               folder        0 Jan 19 20:36 Mastered 1644"
        );
        assertNotNull(parsed);
        assertEquals("Mastered 1644", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());

        parsed = parser.parseFTPEntry(
                "-rwxrwxrwx        0   208143684 208143684 Jan 14 02:13 Dhannya dhannya.rar"
        );
        assertNotNull(parsed);
        assertEquals("Dhannya dhannya.rar", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());

        parsed = parser.parseFTPEntry(
                "drwxr-xr-x               folder        0 Jan 14 16:04 Probeordner");
        assertNotNull(parsed);
        assertEquals("Probeordner", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
    }

}