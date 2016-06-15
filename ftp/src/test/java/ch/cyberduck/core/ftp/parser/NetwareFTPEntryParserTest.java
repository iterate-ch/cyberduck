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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NetwareFTPEntryParserTest {

    private FTPFileEntryParser parser;

    @Before
    public void conigure() {
        this.parser = new FTPParserSelector().getParser("NETWARE  Type : L8");
    }

    /**
     * #1996
     */
    @Test
    public void testDateYearParser() {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "- [RWCEAFMS] wtubbs 24038 May 05 17:57 CIMSscheduler_log_May02_4.txt"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "CIMSscheduler_log_May02_4.txt");
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals("wtubbs", parsed.getUser());
        assertEquals(24038, parsed.getSize());
        assertEquals(Calendar.MAY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(5, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));

        parsed = parser.parseFTPEntry(
                "- [RWCEAFMS] wtubbs 9965 May 01 18:15 CIMSscheduler_log_May01.txt"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "CIMSscheduler_log_May01.txt");
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals("wtubbs", parsed.getUser());
        assertEquals(9965, parsed.getSize());
        assertEquals(Calendar.MAY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(1, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }

    /**
     * #5573
     */
    @Test
    public void testListingWithAbsolutePaths() {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "- [RWCEAFMS] Petersm                             0 May 05  2004 /data/FTP_pub/WelcomeTo_PeakFTP"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "/data/FTP_pub/WelcomeTo_PeakFTP");
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
    }
}