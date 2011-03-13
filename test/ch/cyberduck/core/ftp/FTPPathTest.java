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

import org.apache.commons.net.ftp.FTPFileEntryParser;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

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

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/SunnyD", Path.DIRECTORY_TYPE);
        assertEquals("SunnyD", path.getName());
        assertEquals("/SunnyD", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = path.parseListResponse(list, parser,
                Collections.singletonList(" drwxrwx--x 1 owner group          512 Jun 12 15:40 SunnyD"));

        assertFalse(success);
        assertTrue(list.isEmpty());
    }

    public void testParseSymbolicLink() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/", Path.DIRECTORY_TYPE);
        assertEquals("/", path.getName());
        assertEquals("/", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = path.parseListResponse(list, parser,
                Collections.singletonList("lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 www -> /www/basic/mk"));

        assertTrue(success);
        assertFalse(list.isEmpty());

    }

    public void test3763() throws Exception {
        FTPFileEntryParser parser = new FTPParserFactory().createFileEntryParser("UNIX");

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);
        assertEquals("www", path.getName());
        assertEquals("/www", path.getAbsolute());

        final AttributedList<Path> list = new AttributedList<Path>();
        final boolean success = path.parseListResponse(list, parser,
                Collections.singletonList("lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 /home/mk/www -> /www/basic/mk"));

        assertFalse(success);
        assertTrue(list.isEmpty());

    }

    public void testMlsd() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=file;Perm=awr;Unique=keVO1+8G4; writable",
                "Type=file;Perm=r;Unique=keVO1+IH4;  leading space",
                "Type=dir;Perm=cpmel;Unique=keVO1+7G4; incoming",
        };

        boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(3, children.size());
        assertEquals("writable", children.get(0).getName());
        assertTrue(children.get(0).attributes().isFile());
        assertEquals(" leading space", children.get(1).getName());
        assertTrue(children.get(1).attributes().isFile());
        assertTrue(children.get(2).attributes().isDirectory());
    }

    public void testMlsdCdir() throws Exception {
        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=cdir;Perm=el;Unique=keVO1+ZF4; test", //skipped
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertFalse(success);
            assertEquals(0, children.size());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=cdir;Modify=19990112033515; /iana/assignments/character-set-info", //skipped
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertFalse(success);
            assertEquals(0, children.size());
        }
    }

    public void testMlsdPdir() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=pdir;Perm=e;Unique=keVO1+d?3; ..", //skipped
        };

        boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
        assertFalse(success);
        assertEquals(0, children.size());
    }

    public void testMlsdDirInvalid() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=dir;Unique=aaaaacUYqaaa;Perm=cpmel; /", //skipped
        };

        boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
        assertFalse(success);
        assertEquals(0, children.size());
    }

    public void testSkipParentDir() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; /",
                "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; ..",
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        assertEquals("ftpd.c", children.get(0).getName());
    }

    public void testSize() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        assertEquals(34589, children.get(0).attributes().getSize());
    }

    public void testTimestamp() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=dir;Modify=19990112033045; text" //yyyyMMddHHmmss
        };

        boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        date.set(1999, 0, 12, 3, 30, 45);
        date.set(Calendar.MILLISECOND, 0);
        assertEquals(date.getTime().getTime(), children.get(0).attributes().getModificationDate());
    }

    public void testBrokenMlsd() throws Exception {
        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/Dummies_Infoblaetter", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertFalse(success);
            assertEquals(1, children.size());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
                    "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(2, children.size());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c",
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter"
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(2, children.size());
        }
    }

    public void testParseMlsdMode() throws Exception {
        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "modify=19990307234236;perm=adfr;size=60;type=file;unique=FE03U10001724;UNIX.group=1001;UNIX.mode=0664;UNIX.owner=2000; kalahari.diz"
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("664", children.get(0).attributes().getPermission().getOctalString());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "modify=20090210192929;perm=fle;type=dir;unique=FE03U10006D95;UNIX.group=1001;UNIX.mode=02775;UNIX.owner=2000; tangerine"
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("775", children.get(0).attributes().getPermission().getOctalString());
        }
    }

    public void testParseMlsdSymbolic() throws Exception {
        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/www", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=OS.unix=slink:/foobar;Perm=;Unique=keVO1+4G4; foobar"
            };

            boolean success = path.parseMlsdResponse(children, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("/foobar", children.get(0).getSymlinkTarget());
        }
    }

    public void testParseAbsolutePaths() throws Exception {
        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/data/FTP_pub", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "- [RWCEAFMS] Petersm                             0 May 05  2004 /data/FTP_pub/WelcomeTo_PeakFTP"
            };

            boolean success = path.parseListResponse(children, new FTPParserFactory().createFileEntryParser("NETWARE  Type : L8"),
                    Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("WelcomeTo_PeakFTP", children.get(0).getName());
            assertEquals("/data/FTP_pub", children.get(0).getParent().getAbsolute());
        }
    }

    public void testParseHardlinkCountBadFormat() throws Exception {
        FTPPath path = (FTPPath) PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.FTP, "localhost")),
                "/store/public/brain", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "drwx------+111 mi       public       198 Dec 17 12:29 unsorted"
            };

            boolean success = path.parseListResponse(children, new FTPParserFactory().createFileEntryParser("UNIX"),
                    Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("unsorted", children.get(0).getName());
            assertEquals("/store/public/brain", children.get(0).getParent().getAbsolute());
        }
    }

    public static Test suite() {
        return new TestSuite(FTPPathTest.class);
    }
}
