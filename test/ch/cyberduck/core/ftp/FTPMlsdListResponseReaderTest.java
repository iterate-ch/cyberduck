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

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FTPMlsdListResponseReaderTest extends AbstractTestCase {

    @Test
    public void testMlsd() {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=file;Perm=awr;Unique=keVO1+8G4; writable",
                "Type=file;Perm=r;Unique=keVO1+IH4;  leading space",
                "Type=dir;Perm=cpmel;Unique=keVO1+7G4; incoming",
        };


        boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(3, children.size());
        assertEquals("writable", children.get(0).getName());
        assertTrue(children.get(0).attributes().isFile());
        assertEquals(" leading space", children.get(1).getName());
        assertTrue(children.get(1).attributes().isFile());
        assertTrue(children.get(2).attributes().isDirectory());
    }

    @Test
    public void testMlsdCdir() {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=cdir;Perm=el;Unique=keVO1+ZF4; test", //skipped
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertFalse(success);
            assertEquals(0, children.size());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=cdir;Modify=19990112033515; /iana/assignments/character-set-info", //skipped
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertFalse(success);
            assertEquals(0, children.size());
        }
    }

    @Test
    public void testMlsdPdir() {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=pdir;Perm=e;Unique=keVO1+d?3; ..", //skipped
        };

        boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
        assertFalse(success);
        assertEquals(0, children.size());
    }

    @Test
    public void testMlsdDirInvalid() {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=dir;Unique=aaaaacUYqaaa;Perm=cpmel; /", //skipped
        };

        boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
        assertFalse(success);
        assertEquals(0, children.size());
    }

    public void testSkipParentDir() {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; /",
                "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; ..",
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        assertEquals("ftpd.c", children.get(0).getName());
    }

    @Test
    public void testSize() {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        assertEquals(34589, children.get(0).attributes().getSize());
    }

    @Test
    public void testTimestamp() {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=dir;Modify=19990112033045; text" //yyyyMMddHHmmss
        };

        boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
        assertTrue(success);
        assertEquals(1, children.size());
        Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.set(1999, Calendar.JANUARY, 12, 3, 30, 45);
        date.set(Calendar.MILLISECOND, 0);
        assertEquals(date.getTime().getTime(), children.get(0).attributes().getModificationDate());
    }

    @Test
    public void testBrokenMlsd() {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/Dummies_Infoblaetter", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertFalse(success);
            assertEquals(1, children.size());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
                    "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(2, children.size());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c",
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter"
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(2, children.size());
        }
    }

    @Test
    public void testParseMlsdMode() {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "modify=19990307234236;perm=adfr;size=60;type=file;unique=FE03U10001724;UNIX.group=1001;UNIX.mode=0664;UNIX.owner=2000; kalahari.diz"
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("664", children.get(0).attributes().getPermission().getOctalString());
        }
        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "modify=20090210192929;perm=fle;type=dir;unique=FE03U10006D95;UNIX.group=1001;UNIX.mode=02775;UNIX.owner=2000; tangerine"
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("775", children.get(0).attributes().getPermission().getOctalString());
        }
    }

    @Test
    @Ignore
    public void testParseMlsdSymbolic() {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        FTPPath path = new FTPPath(
                "/www", Path.DIRECTORY_TYPE);

        {
            final AttributedList<Path> children = new AttributedList<Path>();
            String[] replies = new String[]{
                    "Type=OS.unix=slink:/foobar;Perm=;Unique=keVO1+4G4; foobar"
            };

            boolean success = new FTPMlsdListResponseReader().read(children, s, path, null, Arrays.asList(replies));
            assertTrue(success);
            assertEquals(1, children.size());
            assertEquals("/foobar", children.get(0).getSymlinkTarget().getAbsolute());
        }
    }
}
