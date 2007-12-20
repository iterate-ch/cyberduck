package ch.cyberduck.core.ftp.parser;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @version $Id$
 */
public class UnixFTPEntryParserTest extends TestCase {

    public UnixFTPEntryParserTest(String name) {
        super(name);
    }


    public void setUp() throws Exception {

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseFTPEntryExpected() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56 ADMIN_Documentation");
        assertNotNull(parsed);
        assertEquals(parsed.getType(), FTPFile.DIRECTORY_TYPE);
        assertEquals(parsed.getUser(), "user");
        assertEquals(parsed.getGroup(), "ftp");
        assertEquals(parsed.getName(), "ADMIN_Documentation");

        parsed = parser.parseFTPEntry(
                "drwxr--r--   1 user     group          0 Feb 14 18:14 Downloads");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Downloads");
    }

    /**
     * http://trac.cyberduck.ch/ticket/1066
     */
    public void testParseNameWithBeginningWhitespace() {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56  ADMIN_Documentation");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), " ADMIN_Documentation");
    }

    /**
     * http://trac.cyberduck.ch/ticket/1118
     */
    public void testParseNameWithEndingWhitespace() {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56 ADMIN_Documentation ");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "ADMIN_Documentation ");
    }

    /**
     * http://trac.cyberduck.ch/ticket/1076
     *
     * @throws Exception
     */
    public void testSizeWithIndicator() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 9.0M Mar 22 17:44 Cyberduck-2.7.3.dmg"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Cyberduck-2.7.3.dmg");
        assertTrue(parsed.getSize() == (long)(9.0 * 1048576));
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 22);

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 61.8M Mar 7 18:42 GC Wayfinding pics.zip "
        );
        assertNotNull(parsed);
        assertTrue(parsed.getSize() == (long)(61.8 * 1048576));
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 7);

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 172.4k Mar 7 16:01 HEALY071.TXT "
        );
        assertNotNull(parsed);
        assertTrue(parsed.getSize() == (long)(172.4 * 1024));
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 7);
    }

    public void testDoubleWhitespace() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-rwxrwxrwx   1 root     system         9960 Dec 29 2005  dispus");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "dispus");
        assertTrue(parsed.getType() == FTPFile.FILE_TYPE);
        assertEquals(parsed.getUser(), "root");
        assertEquals(parsed.getGroup(), "system");
    }

    public void testLowerCaseMonths() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drwxrwxrwx    41 spinkb  spinkb      1394 jan 21 20:57 Desktop");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Desktop");
        assertTrue(parsed.getType() == FTPFile.DIRECTORY_TYPE);
        assertEquals(parsed.getUser(), "spinkb");
        assertEquals(parsed.getGroup(), "spinkb");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.JANUARY);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 21);
    }

    public void testUpperCaseMonths() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drwxrwxrwx    41 spinkb  spinkb      1394 Feb 21 20:57 Desktop");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Desktop");
        assertTrue(parsed.getType() == FTPFile.DIRECTORY_TYPE);
        assertEquals(parsed.getUser(), "spinkb");
        assertEquals(parsed.getGroup(), "spinkb");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.FEBRUARY);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 21);
    }

    public void testSolarisAcl() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        //#215
        parsed = parser.parseFTPEntry(
                "drwxrwsr-x+ 34 cristol  molvis      3072 Jul 12 20:16 molvis");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "molvis");
        assertTrue(parsed.getType() == FTPFile.DIRECTORY_TYPE);
        assertEquals(parsed.getUser(), "cristol");
        assertEquals(parsed.getGroup(), "molvis");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.JULY);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 12);
    }

    public void testUnknownTimestampFormat() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 hoerspiel hoerspiel  3722053 19. Sep 13:24 Offenbarung 23 - Menschenopfer - 02.mp3"
        );
        assertNotNull(parsed);

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 hoerspiel hoerspiel 10128531 19. Sep 13:24 Offenbarung 23 - Menschenopfer - 01.mp3"
        );
        assertNotNull(parsed);
        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 hoerspiel hoerspiel 11714687 19. Sep 13:25 Offenbarung 23 - Menschenopfer - 08.mp3"
        );
        assertNotNull(parsed);
    }

    public static Test suite() {
        return new TestSuite(UnixFTPEntryParserTest.class);
    }
}
