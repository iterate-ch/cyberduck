package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.cyberduck.core.*;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * @version $Id$
 */
public class FTPPathTest extends AbstractTestCase {

    public FTPPathTest(String name) {
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

    public void test3243() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/SunnyD", Path.DIRECTORY_TYPE);
        assertEquals("SunnyD", path.getName());
        assertEquals("/SunnyD", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = path.parse(list, parser,
                new BufferedReader(new StringReader(" drwxrwx--x 1 owner group          512 Jun 12 15:40 SunnyD")));

        assertFalse(success);
        assertTrue(list.isEmpty());
    }

    public void testParseSymbolicLink() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/", Path.DIRECTORY_TYPE);
        assertEquals("/", path.getName());
        assertEquals("/", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = path.parse(list, parser,
                new BufferedReader(new StringReader("lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 www -> /www/basic/mk")));

        assertTrue(success);
        assertFalse(list.isEmpty());

    }

    public void test3763() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPFile parsed = null;

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);
        assertEquals("www", path.getName());
        assertEquals("/www", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = path.parse(list, parser,
                new BufferedReader(new StringReader("lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 /home/mk/www -> /www/basic/mk")));

        assertFalse(success);
        assertTrue(list.isEmpty());

    }

    public static Test suite() {
        return new TestSuite(FTPPathTest.class);
    }
}
