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

public class vsFTPdEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void configure() {
        this.parser = new FTPParserSelector().getParser("UNIX Type: L8");
    }

    @Test
    public void testParse() throws Exception {
        FTPFile parsed;

        // #5437
        parsed = parser.parseFTPEntry(
                "-rw-r--r--    1 3642     3643          106 Nov 15 22:20 index.html"
        );
        assertNotNull(parsed);
        assertEquals("index.html", parsed.getName());
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals(106, parsed.getSize());
        assertEquals(Calendar.NOVEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(15, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
    }
}