package org.apache.commons.net.ftp.parser;

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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import ch.cyberduck.core.ftp.FTPParserFactory;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import java.util.Calendar;

/**
 * @version $Id$
 */
public class UnixFTPEntryParserTest extends TestCase
{

    public UnixFTPEntryParserTest(String name)
    {
        super(name);
    }

    private FTPFileEntryParser parser;


    public void setUp() throws Exception
    {
        this.parser = new FTPParserFactory().createFileEntryParser("UNIX");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testParseFTPEntryExpected() throws Exception
    {
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
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "drw-rw-rw-   1 user      ftp             0  Mar 11 20:56 ADMIN_Documentation ");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "ADMIN_Documentation ");
    }

    /**
     * http://trac.cyberduck.ch/ticket/1076
     * @throws Exception
     */
    public void testSizeWithIndicator() throws Exception {
        FTPFile parsed = null;

        parsed = parser.parseFTPEntry(
                "-rw-rw-rw- 1 ftp operator 9.0M Mar 22 17:44 Cyberduck-2.7.3.dmg");
        assertNotNull(parsed);
        assertEquals(parsed.getName(), "Cyberduck-2.7.3.dmg");
        assertTrue(parsed.getSize() == 9.0*(2^20));
    }

    public void testDoubleWhitespace() throws Exception {
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

    public static Test suite()
    {
        return new TestSuite(UnixFTPEntryParserTest.class);
    }
}
