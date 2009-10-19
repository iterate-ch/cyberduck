package ch.cyberduck.core.ftp.parser;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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
public class HPTru64ParserTest extends AbstractTestCase {

    public HPTru64ParserTest(String name) {
        super(name);
    }

    private FTPFileEntryParser parser;


    @Override
    public void setUp() {
        super.setUp();
        this.parser = new FTPParserFactory().createFileEntryParser("UNIX");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * http://trac.cyberduck.ch/ticket/2246
     *
     * @throws Exception
     */
    public void testParse() throws Exception {
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drwxr-xr-x   7 ToysPKG  advertise   8192 Jun 24 11:58 Private Label Mock"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Private Label Mock");
        assertTrue(parsed.getType() == FTPFile.DIRECTORY_TYPE);
        assertEquals("ToysPKG", parsed.getUser());
        assertEquals("advertise", parsed.getGroup());
        assertEquals(8192, parsed.getSize());
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.JUNE);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 24);

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 ToysPKG  advertise24809879 Jun 25 10:54 TRU-Warning Guide Master CD.sitx"
        );
        assertNull(parsed);
    }

    public static Test suite() {
        return new TestSuite(FreeboxFTPEntryParserTest.class);
    }
}