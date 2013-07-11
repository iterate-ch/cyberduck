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

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class FTPMlsdListResponseReaderTest extends AbstractTestCase {

    @Test
    public void testMlsd() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=file;Perm=awr;Unique=keVO1+8G4; writable",
                "Type=file;Perm=r;Unique=keVO1+IH4;  leading space",
                "Type=dir;Perm=cpmel;Unique=keVO1+7G4; incoming",
        };


        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(3, children.size());
        assertEquals("writable", children.get(0).getName());
        assertTrue(children.get(0).attributes().isFile());
        assertEquals(" leading space", children.get(1).getName());
        assertTrue(children.get(1).attributes().isFile());
        assertTrue(children.get(2).attributes().isDirectory());
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdCdir1() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=cdir;Perm=el;Unique=keVO1+ZF4; test", //skipped
        };
        new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdCdir2() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=cdir;Modify=19990112033515; /iana/assignments/character-set-info", //skipped
        };
        new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdPdir() throws Exception {
        final AttributedList<Path> children = new AttributedList<Path>();

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=pdir;Perm=e;Unique=keVO1+d?3; ..", //skipped
        };
        new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdDirInvalid() throws Exception {

        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=dir;Unique=aaaaacUYqaaa;Perm=cpmel; /", //skipped
        };
        new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
    }

    @Test
    public void testSkipParentDir() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; /",
                "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; ..",
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("ftpd.c", children.get(0).getName());
    }

    @Test
    public void testSize() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        String[] replies = new String[]{
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals(34589, children.get(0).attributes().getSize());
    }

    @Test
    public void testTimestamp() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);

        // Tuesday, January 12, 1999 3:30:45 AM GMT
        String[] replies = new String[]{
                "Type=dir;Modify=19990112033045; text" //yyyyMMddHHmmss
        };
        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals(916111845000L, children.get(0).attributes().getModificationDate());
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        date.set(1999, Calendar.JANUARY, 12, 3, 30, 45);
        date.set(Calendar.MILLISECOND, 0);
        assertEquals(date.getTimeInMillis(), children.get(0).attributes().getModificationDate());
    }

    @Test
    public void testParseTimestamp() throws Exception {
        // Pass UTC time
        final long time = new FTPMlsdListResponseReader().parseTimestamp("20130709111201");
        assertEquals(1373368321000L, time);
    }

    @Test
    public void testParseTimestampInvalid() throws Exception {
        final long time = new FTPMlsdListResponseReader().parseTimestamp("2013");
        assertEquals(-1L, time);
    }

    @Test(expected = FTPInvalidListException.class)
    public void testBrokenMlsd() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/Dummies_Infoblaetter", Path.DIRECTORY_TYPE);
        String[] replies = new String[]{
                "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
        };
        new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
    }

    public void testDir() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/Dummies_Infoblaetter", Path.DIRECTORY_TYPE);
        {
            String[] replies = new String[]{
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
                    "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
            };

            final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
            assertEquals(2, children.size());
        }
        {
            String[] replies = new String[]{
                    "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c",
                    "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter"
            };

            final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
            assertEquals(2, children.size());
        }
    }

    @Test
    public void testParseMlsdMode664() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);
        String[] replies = new String[]{
                "modify=19990307234236;perm=adfr;size=60;type=file;unique=FE03U10001724;UNIX.group=1001;UNIX.mode=0664;UNIX.owner=2000; kalahari.diz"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("664", children.get(0).attributes().getPermission().getOctalString());
    }

    @Test
    public void testParseMlsdMode775() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);
        String[] replies = new String[]{
                "modify=20090210192929;perm=fle;type=dir;unique=FE03U10006D95;UNIX.group=1001;UNIX.mode=02775;UNIX.owner=2000; tangerine"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("775", children.get(0).attributes().getPermission().getOctalString());
    }

    @Test
    public void testParseMlsdSymbolic() throws Exception {
        final FTPSession s = new FTPSession(new Host(Protocol.FTP, "localhost"));
        Path path = new Path(
                "/www", Path.DIRECTORY_TYPE);
        String[] replies = new String[]{
                "Type=OS.unix=slink:/foobar;Perm=;Unique=keVO1+4G4; foobar"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(s, path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("/www/foobar", children.get(0).getAbsolute());
        assertEquals("/foobar", children.get(0).getSymlinkTarget().getAbsolute());
    }
}
