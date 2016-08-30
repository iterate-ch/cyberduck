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

import ch.cyberduck.core.ftp.FTPParserSelector;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.*;

public class UnixFTPEntryParserTest {

    @Test
    public void testParseTimestamp() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  DEC 11 20:56 ADMIN_Documentation");
        assertNotNull(parsed);
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.DECEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(11, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(20, parsed.getTimestamp().get(Calendar.HOUR_OF_DAY));
        assertEquals(56, parsed.getTimestamp().get(Calendar.MINUTE));

        parsed = parser.parseFTPEntry(
                "drwxr-xr-x    3 ftp      ftp           512 Mar 15  2004 doc");
        assertNotNull(parsed);
        assertNotNull(parsed.getTimestamp());
        assertEquals(2004, parsed.getTimestamp().get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(15, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));

        parsed = parser.parseFTPEntry(
                "drwxrwxr-x    2 ftp      ftp           512 Oct 23  2007 aurox");
        assertNotNull(parsed);
        assertNotNull(parsed.getTimestamp());
        assertEquals(2007, parsed.getTimestamp().get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(23, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParseFTPEntryExpected() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56 ADMIN_Documentation");
        assertNotNull(parsed);
        assertEquals(parsed.getType(), FTPFile.DIRECTORY_TYPE);
        assertEquals("user", parsed.getUser());
        assertEquals("ftp", parsed.getGroup());
        assertEquals("ADMIN_Documentation", parsed.getName());

        parsed = parser.parseFTPEntry(
                "drwxr--r--   1 user     group          0 Feb 14 18:14 Downloads");
        assertNotNull(parsed);
        assertEquals("Downloads", parsed.getName());
    }

    /**
     * http://trac.cyberduck.ch/ticket/1066
     */
    @Test
    public void testParseNameWithBeginningWhitespace() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56  ADMIN_Documentation");
        assertNotNull(parsed);
        assertEquals(" ADMIN_Documentation", parsed.getName());
    }

    /**
     * http://trac.cyberduck.ch/ticket/1118
     */
    @Test
    public void testParseNameWithEndingWhitespace() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56 ADMIN_Documentation ");
        assertNotNull(parsed);
        assertEquals("ADMIN_Documentation ", parsed.getName());
    }

    /**
     * http://trac.cyberduck.ch/ticket/1076
     *
     * @
     */
    @Test
    public void testSizeWithIndicator() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 9.0M Mar 22 17:44 Cyberduck-2.7.3.dmg"
        );
        assertNotNull(parsed);
        assertTrue(parsed.isFile());
        assertEquals("Cyberduck-2.7.3.dmg", parsed.getName());
        assertEquals((long) (9.0 * 1048576), parsed.getSize());
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.MARCH, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(22, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 61.8M Mar 7 18:42 GC Wayfinding pics.zip "
        );
        assertNotNull(parsed);
        assertTrue(parsed.isFile());
        assertEquals((long) (61.8 * 1048576), parsed.getSize());
        assertEquals("ftp", parsed.getUser());
        assertEquals("operator", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.MARCH, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(7, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 172.4k Mar 7 16:01 HEALY071.TXT "
        );
        assertNotNull(parsed);
        assertTrue(parsed.isFile());
        assertEquals((long) (172.4 * 1024), parsed.getSize());
        assertEquals(parsed.getUser(), "ftp");
        assertEquals(parsed.getGroup(), "operator");
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.MARCH, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(7, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testCurrentYear() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 20708    205             194 Oct 17 14:40 D3I0_805.fixlist");
        assertNotNull(parsed);
        assertTrue(parsed.isFile());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(17, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(14, parsed.getTimestamp().get(Calendar.HOUR_OF_DAY));
        assertEquals(40, parsed.getTimestamp().get(Calendar.MINUTE));
    }

    /**
     * http://trac.cyberduck.ch/ticket/143
     */
    @Test
    public void testLeadingWhitespace() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "-rw-r--r--   1 20708    205         3553312 Feb 18 2005  D3I0_515.fmr");
        assertNotNull(parsed);
        assertTrue(parsed.isFile());
        assertEquals("D3I0_515.fmr", parsed.getName());
        assertEquals("20708", parsed.getUser());
        assertEquals("205", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.FEBRUARY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(18, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(2005, parsed.getTimestamp().get(Calendar.YEAR));

        parsed = parser.parseFTPEntry(
                "drwxr-sr-x  14 17037    209            4096 Oct  6 2000  v3r7");
        assertNotNull(parsed);
        assertTrue(parsed.isDirectory());
        assertEquals("v3r7", parsed.getName());
        assertEquals("17037", parsed.getUser());
        assertEquals("209", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(6, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(2000, parsed.getTimestamp().get(Calendar.YEAR));

        // #2895
        parsed = parser.parseFTPEntry(
                "-rwx------ 1 user group          38635 Jul 13 2006  users.xml");
        assertNotNull(parsed);
        assertEquals(FTPFile.FILE_TYPE, parsed.getType());
        assertEquals("users.xml", parsed.getName());
        assertEquals("user", parsed.getUser());
        assertEquals("group", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.JULY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(13, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(2006, parsed.getTimestamp().get(Calendar.YEAR));
    }

    @Test
    public void testLowerCaseMonths() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwxrwxrwx    41 spinkb  spinkb      1394 jan 21 20:57 Desktop");
        assertNotNull(parsed);
        assertEquals("Desktop", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals("spinkb", parsed.getUser());
        assertEquals("spinkb", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.JANUARY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(21, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testUpperCaseMonths() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwxrwxrwx    41 spinkb  spinkb      1394 Feb 21 20:57 Desktop");
        assertNotNull(parsed);
        assertEquals("Desktop", parsed.getName());
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals("spinkb", parsed.getUser());
        assertEquals("spinkb", parsed.getGroup());
        assertEquals(Calendar.FEBRUARY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(21, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSolarisAcl() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        //#215
        parsed = parser.parseFTPEntry(
                "drwxrwsr-x+ 34 cristol  molvis      3072 Jul 12 20:16 molvis");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "molvis");
        assertEquals(FTPFile.DIRECTORY_TYPE, parsed.getType());
        assertEquals("cristol", parsed.getUser());
        assertEquals("molvis", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.JULY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(12, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
    }

    public void testUnknownTimestampFormat() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

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

    @Test
    @Ignore
    public void testLeapYear() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwxr--r--   1 user     group          0 Feb 29 18:14 Downloads"
        );
        assertNotNull(parsed);
        assertNotNull(parsed.getTimestamp());
    }

    @Test
    public void testCarriageReturn() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        // #1521
        parsed = parser.parseFTPEntry(
                "drwxr--r--   1 user     group          0 Feb 29 18:14 Icon\r"
        );
        assertNotNull(parsed);
        assertEquals("Icon\r", parsed.getName());
    }

    @Test
    public void testSetuid() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwsr--r--   1 user     group          0 Feb 29 18:14 Filename"
        );
        assertNotNull(parsed);
        assertTrue(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));

        parsed = parser.parseFTPEntry(
                "drwSr--r--   1 user     group          0 Feb 29 18:14 Filename"
        );
        assertNotNull(parsed);
        assertFalse(parsed.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Test
    public void testSetgid() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwxr-sr--   1 user     group          0 Feb 29 18:14 Filename"
        );
        assertNotNull(parsed);
        assertTrue(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));

        parsed = parser.parseFTPEntry(
                "drwxr-Sr--   1 user     group          0 Feb 29 18:14 Filename"
        );
        assertNotNull(parsed);
        assertFalse(parsed.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Test
    public void testStickyBit() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        FTPFile parsed;

        parsed = parser.parseFTPEntry(
                "drwxr--r-t   1 user     group          0 Feb 29 18:14 Filename"
        );
        assertNotNull(parsed);
        assertTrue(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        parsed = parser.parseFTPEntry(
                "drwxr--r-T   1 user     group          0 Feb 29 18:14 Filename"
        );
        assertNotNull(parsed);
        assertFalse(parsed.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Test
    public void testWindowsNTSystem() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("Windows_NT version 5.0");

        FTPFile parsed;

        // #5505
        parsed = parser.parseFTPEntry(
                "drwxrwxrwx   1 owner    group               0 Dec  5  0:45 adele.handmadebyflloyd.com"
        );
        assertNotNull(parsed);
        assertEquals("adele.handmadebyflloyd.com", parsed.getName());
        assertEquals("owner", parsed.getUser());
        assertEquals("group", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.DECEMBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(5, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));

        // #5505
        parsed = parser.parseFTPEntry(
                "drwxrwxrwx   1 owner    group               0 Jan 22  2009 contact"
        );
        assertNotNull(parsed);
        assertEquals("contact", parsed.getName());
        assertEquals("owner", parsed.getUser());
        assertEquals("group", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.JANUARY, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(22, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(2009, parsed.getTimestamp().get(Calendar.YEAR));
    }

    @Test
    public void testMVSParser() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("MVS is the operating system of this server. FTP Server is running on z/OS.");

        FTPFile parsed;

        final String entry = "drwxr-xr-x   6 START2   SYS1        8192 Oct 28  2008 ADCD";
        parser.preParse(new ArrayList<String>(Arrays.asList("total 66", entry)));
        // #7717
        parsed = parser.parseFTPEntry(entry);
        assertNotNull(parsed);
        assertEquals("ADCD", parsed.getName());
        assertEquals("START2", parsed.getUser());
        assertEquals("SYS1", parsed.getGroup());
        assertNotNull(parsed.getTimestamp());
        assertEquals(Calendar.OCTOBER, parsed.getTimestamp().get(Calendar.MONTH));
        assertEquals(28, parsed.getTimestamp().get(Calendar.DAY_OF_MONTH));
        assertEquals(2008, parsed.getTimestamp().get(Calendar.YEAR));
    }
}
