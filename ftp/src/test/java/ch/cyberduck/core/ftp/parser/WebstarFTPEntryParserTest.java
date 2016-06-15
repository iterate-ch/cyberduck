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

public class WebstarFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void conigure() {
        this.parser = new FTPParserSelector().getParser("MACOS WebSTAR FTP");
    }

    @Test
    public void testParse() throws Exception {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "-rwx------          17      332      640 Dec 20 08:54 file 1"
        );
        assertNotNull(parsed);
        assertEquals("file 1", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals(640, parsed.getSize());

        parsed = parser.parseFTPEntry(
                "drwx------             folder          2 Dec 20 08:55 folder1"
        );
        assertNotNull(parsed);
        assertEquals("folder1", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals(Calendar.DECEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(20, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }
}