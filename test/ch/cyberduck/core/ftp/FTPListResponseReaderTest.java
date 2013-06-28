package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class FTPListResponseReaderTest extends AbstractTestCase {

    @Test
    public void test3243() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(s, "/SunnyD", Path.DIRECTORY_TYPE);
        assertEquals("SunnyD", path.getName());
        assertEquals("/SunnyD", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = new FTPListResponseReader().read(list, s, path, parser,
                Collections.singletonList(" drwxrwx--x 1 owner group          512 Jun 12 15:40 SunnyD"));

        assertFalse(success);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testParseSymbolicLink() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(s, "/", Path.DIRECTORY_TYPE);
        assertEquals("/", path.getName());
        assertEquals("/", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = new FTPListResponseReader().read(list, s, path, parser,
                Collections.singletonList("lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 www -> /www/basic/mk"));

        assertTrue(success);
        assertFalse(list.isEmpty());
        assertTrue(list.get(0).attributes().isSymbolicLink());
        assertEquals("/www/basic/mk", list.get(0).getSymlinkTarget().getAbsolute());
    }

    @Test
    public void test3763() {
        FTPFileEntryParser parser = new FTPParserSelector().getParser("UNIX");

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(s, "/www", Path.DIRECTORY_TYPE);
        assertEquals("www", path.getName());
        assertEquals("/www", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = new FTPListResponseReader().read(list, s, path, parser,
                Collections.singletonList("lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 /home/mk/www -> /www/basic/mk"));

        assertFalse(success);
        assertTrue(list.isEmpty());
    }

    @Test
    @Ignore
    public void testParseHardlinkCountBadFormat() {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));

        FTPPath path = new FTPPath(s,
                "/store/public/brain", Path.DIRECTORY_TYPE);

        final AttributedList<Path> list = new AttributedList<Path>();
        String[] replies = new String[]{
                "drwx------+111 mi       public       198 Dec 17 12:29 unsorted"
        };

        final CompositeFileEntryParser parser = new FTPParserSelector().getParser("UNIX");
        final boolean success = new FTPListResponseReader().read(list, s, path, parser, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, list.size());
        assertEquals("unsorted", list.get(0).getName());
        assertEquals("/store/public/brain", list.get(0).getParent().getAbsolute());
    }


    @Test
    public void testParseAbsolutePaths() {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(s,
                "/data/FTP_pub", Path.DIRECTORY_TYPE);

        final AttributedList<Path> children = new AttributedList<Path>();
        String[] replies = new String[]{
                "- [RWCEAFMS] Petersm                             0 May 05  2004 /data/FTP_pub/WelcomeTo_PeakFTP"
        };

        boolean success = new FTPListResponseReader().read(children, s, path, new FTPParserSelector().getParser("NETWARE  Type : L8"),
                Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        assertEquals("WelcomeTo_PeakFTP", children.get(0).getName());
        assertEquals("/data/FTP_pub", children.get(0).getParent().getAbsolute());
    }
}
