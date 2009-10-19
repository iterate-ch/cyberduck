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

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.ftp.FTPParserFactory;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import java.util.Calendar;

/**
 * @version $Id$
 */
public class StingrayFTPEntryParserTest extends AbstractTestCase {

    public StingrayFTPEntryParserTest(String name) {
        super(name);
    }

    private FTPFileEntryParser parser;

    @Override
    public void setUp() {
        super.setUp();
        this.parser = new FTPParserFactory().createFileEntryParser("MACOS");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * http://trac.cyberduck.ch/ticket/1198
     */
    public void testFile() throws Exception {
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-r--r--r--          0     165100     165100 Aug  1 10:24 grau2.tif"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "grau2.tif");
        assertTrue(parsed.getType() == FTPFile.FILE_TYPE);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.AUGUST);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 1);
        assertTrue(parsed.getTimestamp().get(Calendar.HOUR_OF_DAY) == 10);
        assertTrue(parsed.getTimestamp().get(Calendar.MINUTE) == 24);
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
    public void testFolder() throws Exception {
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "dr--r--r--                folder          0 Aug  1 10:18 TestCyberduck"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "TestCyberduck");
        assertTrue(parsed.getType() == FTPFile.DIRECTORY_TYPE);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.AUGUST);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 1);
        assertTrue(parsed.getTimestamp().get(Calendar.HOUR_OF_DAY) == 10);
        assertTrue(parsed.getTimestamp().get(Calendar.MINUTE) == 18);
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

    public static Test suite() {
        return new TestSuite(StingrayFTPEntryParserTest.class);
    }
}
