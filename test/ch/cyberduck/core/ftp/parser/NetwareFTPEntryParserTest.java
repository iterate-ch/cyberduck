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
 * @version $Id:$
 */
public class NetwareFTPEntryParserTest extends AbstractTestCase {

    public NetwareFTPEntryParserTest(String name) {
        super(name);
    }

    private FTPFileEntryParser parser;


    @Override
    public void setUp() {
        super.setUp();
        this.parser = new FTPParserFactory().createFileEntryParser("NETWARE");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * http://trac.cyberduck.ch/ticket/1996
     *
     * @throws Exception
     */
    public void testParse() throws Exception {
        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "- [RWCEAFMS] wtubbs 24038 May 05 17:57 CIMSscheduler_log_May02_4.txt"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "CIMSscheduler_log_May02_4.txt");
        assertTrue(parsed.getType() == FTPFile.FILE_TYPE);
        assertEquals("wtubbs", parsed.getUser());
        assertEquals(24038, parsed.getSize());
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        assertTrue(parsed.getTimestamp().get(Calendar.YEAR) == year);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MAY);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 5);

        parsed = parser.parseFTPEntry(
                "- [RWCEAFMS] wtubbs 9965 May 01 18:15 CIMSscheduler_log_May01.txt"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "CIMSscheduler_log_May01.txt");
        assertTrue(parsed.getType() == FTPFile.FILE_TYPE);
        assertEquals("wtubbs", parsed.getUser());
        assertEquals(9965, parsed.getSize());
        assertTrue(parsed.getTimestamp().get(Calendar.YEAR) == year);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MAY);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 1);
    }

    public static Test suite() {
        return new TestSuite(NetwareFTPEntryParserTest.class);
    }
}