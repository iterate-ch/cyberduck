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
public class UnixFTPEntryParserTest extends AbstractTestCase {

    public UnixFTPEntryParserTest(String name) {
        super(name);
    }


    @Override
    public void setUp() {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseTimestamp() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  DEC 11 20:56 ADMIN_Documentation");
        assertNotNull(parsed);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.DECEMBER);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 11);
        assertTrue(parsed.getTimestamp().get(Calendar.HOUR_OF_DAY) == 20);
        assertTrue(parsed.getTimestamp().get(Calendar.MINUTE) == 56);

        parsed = parser.parseFTPEntry(
                "drwxr-xr-x    3 ftp      ftp           512 Mar 15  2004 doc");
        assertNotNull(parsed);
        assertTrue(parsed.getTimestamp().get(Calendar.YEAR) == 2004);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 15);

        parsed = parser.parseFTPEntry(
                "drwxrwxr-x    2 ftp      ftp           512 Oct 23  2007 aurox");
        assertNotNull(parsed);
        assertTrue(parsed.getTimestamp().get(Calendar.YEAR) == 2007);
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.OCTOBER);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 23);
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
        assertTrue(parsed.getSize() == (long) (9.0 * 1048576));
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 22);

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 61.8M Mar 7 18:42 GC Wayfinding pics.zip "
        );
        assertNotNull(parsed);
        assertTrue(parsed.getSize() == (long) (61.8 * 1048576));
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 7);

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 172.4k Mar 7 16:01 HEALY071.TXT "
        );
        assertNotNull(parsed);
        assertTrue(parsed.getSize() == (long) (172.4 * 1024));
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.MARCH);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 7);
    }

    /**
     * http://trac.cyberduck.ch/ticket/143
     *
     * @throws Exception
     */
    public void testLeadingWhitespace() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 20708    205             194 Oct 17 14:40 D3I0_805.fixlist");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "D3I0_805.fixlist");
        assertEquals(parsed.getUser(), "20708");
        assertEquals(parsed.getGroup(), "205");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.OCTOBER);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 17);
        assertTrue(parsed.getTimestamp().get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR));
        assertTrue(parsed.getTimestamp().get(Calendar.HOUR_OF_DAY) == 14);
        assertTrue(parsed.getTimestamp().get(Calendar.MINUTE) == 40);

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 20708    205         3553312 Feb 18 2005  D3I0_515.fmr");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "D3I0_515.fmr");
        assertEquals(parsed.getUser(), "20708");
        assertEquals(parsed.getGroup(), "205");
        assertTrue(parsed.getTimestamp().get(Calendar.MONTH) == Calendar.FEBRUARY);
        assertTrue(parsed.getTimestamp().get(Calendar.DAY_OF_MONTH) == 18);
        assertTrue(parsed.getTimestamp().get(Calendar.YEAR) == 2005);
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
        assertEquals(parsed.getName(), "Offenbarung 23 - Menschenopfer - 02.mp3");

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 hoerspiel hoerspiel 10128531 19. Sep 13:24 Offenbarung 23 - Menschenopfer - 01.mp3"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Offenbarung 23 - Menschenopfer - 01.mp3");
        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 hoerspiel hoerspiel 11714687 19. Sep 13:25 Offenbarung 23 - Menschenopfer - 08.mp3"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Offenbarung 23 - Menschenopfer - 08.mp3");

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 www-data www-data      10089849 Dec 20 09:30 Stone Catalog"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Stone Catalog");
        assertEquals(parsed.getUser(), "www-data");
        assertEquals(parsed.getGroup(), "www-data");
        assertEquals(parsed.getSize(), 10089849);

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 www-data www-data      34524204 Dec 20 13:41 Winter 2008 Newsletter.sit"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Winter 2008 Newsletter.sit");
        assertEquals(parsed.getUser(), "www-data");
        assertEquals(parsed.getGroup(), "www-data");
        assertEquals(parsed.getSize(), 34524204);
    }

    public void testLeapYear() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drwxr--r--   1 user     group          0 Feb 29 18:14 Downloads"
        );
        assertNotNull(parsed);
        assertEquals(parsed.getTimestamp().get(Calendar.MONTH), Calendar.FEBRUARY);
        assertEquals(parsed.getTimestamp().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.YEAR));

    }

    public static Test suite() {
        return new TestSuite(UnixFTPEntryParserTest.class);
    }
}
