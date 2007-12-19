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

import ch.cyberduck.core.ftp.FTPParserFactory;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFile;

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * @version $Id:$
 */
public class FreeboxFTPEntryParserTest extends TestCase {

    public FreeboxFTPEntryParserTest(String name) {
        super(name);
    }

    private FTPFileEntryParser parser;


    public void setUp() throws Exception {
        this.parser = new FTPParserFactory().createFileEntryParser("UNIX");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParse() throws Exception {
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1  freebox  freebox 75906880 Sep 08 06:33 Direct 8 - Gym direct - 08-09-2007 08h30 1h08m.ts"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Direct 8 - Gym direct - 08-09-2007 08h30 1h08m.ts");
        assertTrue(parsed.getType() == FTPFile.FILE_TYPE);
        assertEquals("freebox", parsed.getUser());
        assertEquals("freebox", parsed.getGroup());
        assertEquals(75906880, parsed.getSize());
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.SEPTEMBER);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 8);
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1  freebox  freebox 2064965868 Apr 15 21:17 M6 - Capital 15-04-2007 21h37 1h40m.ts"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "M6 - Capital 15-04-2007 21h37 1h40m.ts");
        assertTrue(parsed.getType() == FTPFile.FILE_TYPE);
        assertEquals("freebox", parsed.getUser());
        assertEquals("freebox", parsed.getGroup());
        assertEquals(75906880, parsed.getSize());
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.SEPTEMBER);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 8);
    }

    public static Test suite() {
        return new TestSuite(FreeboxFTPEntryParserTest.class);
    }
}
