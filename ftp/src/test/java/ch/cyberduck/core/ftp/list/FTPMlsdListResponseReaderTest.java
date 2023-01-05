package ch.cyberduck.core.ftp.list;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class FTPMlsdListResponseReaderTest {

    @Test
    public void testMlsd() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "Type=file;Perm=awr;Unique=keVO1+8G4; writable",
            "Type=file;Perm=r;Unique=keVO1+IH4;  leading space",
            "Type=dir;Perm=cpmel;Unique=keVO1+7G4; incoming",
        };


        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(3, children.size());
        assertEquals("writable", children.get(0).getName());
        assertTrue(children.get(0).isFile());
        assertTrue(children.get(0).attributes().getPermission().isReadable());
        assertTrue(children.get(0).attributes().getPermission().isWritable());
        assertEquals(" leading space", children.get(1).getName());
        assertTrue(children.get(1).isFile());
        assertEquals(Permission.EMPTY, children.get(1).attributes().getPermission());
        assertTrue(children.get(2).isDirectory());
        assertEquals(Permission.EMPTY, children.get(2).attributes().getPermission());
    }

    @Test
    public void testParsePermissions() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
                "Type=dir;Modify=20171202151917;Unique=04c800e7ef006c54;Perm=cmpdfe; progrocklists",
        };
        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertTrue(children.get(0).isDirectory());
        assertTrue(children.get(0).attributes().getPermission().isReadable());
        assertTrue(children.get(0).attributes().getPermission().isWritable());
        assertTrue(children.get(0).attributes().getPermission().isExecutable());
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdCdir1() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "Type=cdir;Perm=el;Unique=keVO1+ZF4; test", //skipped
        };
        new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdCdir2() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "Type=cdir;Modify=19990112033515; /iana/assignments/character-set-info", //skipped
        };
        new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
    }

    @Test(expected = FTPInvalidListException.class)
    public void testMlsdPdir() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "Type=pdir;Perm=e;Unique=keVO1+d?3; ..", //skipped
        };
        new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
    }

    @Test
    public void testSkipParentDir() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; /",
            "Type=pdir;Unique=aaaaacUYqaaa;Perm=cpmel; ..",
            "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("ftpd.c", children.get(0).getName());
    }

    @Test
    public void testEmptyDir() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{};

        final AttributedList<Path> children = new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
        assertEquals(0, children.size());
    }

    @Test
    public void testSize() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals(34589, children.get(0).attributes().getSize());
    }

    @Test
    public void testTimestamp() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));

        // Tuesday, January 12, 1999 3:30:45 AM GMT
        String[] replies = new String[]{
                "Type=dir;Modify=19990112033045; text" //yyyyMMddHHmmss
        };
        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals(916111845000L, children.get(0).attributes().getModificationDate());
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        date.set(1999, Calendar.JANUARY, 12, 3, 30, 45);
        date.set(Calendar.MILLISECOND, 0);
        assertEquals(date.getTimeInMillis(), children.get(0).attributes().getModificationDate());
    }

    @Test
    public void testParseTimestamp() {
        // Pass UTC time
        final long time = new FTPMlsdListResponseReader().parseTimestamp("20130709111201");
        assertEquals(1373368321000L, time);
    }

    @Test
    public void testParseTimestampInvalid() {
        final long time = new FTPMlsdListResponseReader().parseTimestamp("2013");
        assertEquals(-1L, time);
    }

    @Test(expected = FTPInvalidListException.class)
    public void testBrokenMlsd() throws Exception {
        Path path = new Path(
            "/Dummies_Infoblaetter", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
            "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
        };
        new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
    }

    public void testDir() throws Exception {
        Path path = new Path(
            "/Dummies_Infoblaetter", EnumSet.of(Path.Type.directory));
        {
            String[] replies = new String[]{
                "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter",
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c"
            };

            final AttributedList<Path> children = new FTPMlsdListResponseReader()
                    .read(path, Arrays.asList(replies));
            assertEquals(2, children.size());
        }
        {
            String[] replies = new String[]{
                "Type=file;Unique=aaab8bUYqaaa;Perm=rf;Size=34589; ftpd.c",
                "Type=dir;Modify=20101209140859;Win32.ea=0x00000010; Dummies_Infoblaetter"
            };

            final AttributedList<Path> children = new FTPMlsdListResponseReader()
                    .read(path, Arrays.asList(replies));
            assertEquals(2, children.size());
        }
    }

    @Test
    public void testParseMlsdMode664() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
            "modify=19990307234236;perm=adfr;size=60;type=file;unique=FE03U10001724;UNIX.group=1001;UNIX.mode=0664;UNIX.owner=2000; kalahari.diz"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("664", children.get(0).attributes().getPermission().getMode());
    }

    @Test
    public void testParseMlsdMode775() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
            "modify=20090210192929;perm=fle;type=dir;unique=FE03U10006D95;UNIX.group=1001;UNIX.mode=02775;UNIX.owner=2000; tangerine"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("2775", children.get(0).attributes().getPermission().getMode());
    }

    @Test
    public void testParseMlsdSymbolic() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
            "Type=OS.unix=slink:/foobar;Perm=;Unique=keVO1+4G4; foobar"
        };

        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(1, children.size());
        assertEquals("/www/foobar", children.get(0).getAbsolute());
        assertEquals("/foobar", children.get(0).getSymlinkTarget().getAbsolute());
    }

    @Test(expected = FTPInvalidListException.class)
    public void testParseMlsdSymbolicMissingTarget() throws Exception {
        Path path = new Path(
            "/www", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
            "type=OS.unix=slink:;size=11;modify=20190522005707;UNIX.mode=0777;UNIX.uid=1677;UNIX.gid=1676;unique=841g5e0003; www"
        };
        new FTPMlsdListResponseReader().read(path, Arrays.asList(replies));
    }

    @Test
    @Ignore
    public void testParseSlashInFilename() throws Exception {
        Path path = new Path("/www", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
                "type=dir;modify=20140315210350; Gozo 2013/2014",
                "type=dir;modify=20140315210350; Tigger & Friends"
        };
        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
        assertEquals(2, children.size());
        assertEquals("/www/Gozo 2013/2014", children.get(0).getAbsolute());
        assertEquals("Gozo 2013/2014", children.get(0).getName());
    }

    @Test(expected = FTPInvalidListException.class)
    public void test8053() throws Exception {
        Path path = new Path("/", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
            "type=OS.unix=slink:;size=11;modify=20140506165021;UNIX.mode=0777;UNIX.uid=1144;UNIX.gid=1144;unique=fd51g2dc0020; www"
        };
        new FTPMlsdListResponseReader()
                .read(path, Arrays.asList(replies));
    }

    @Test
    public void testSkipCurrentAndParentDir() throws Exception {
        Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        String[] replies = new String[]{
                "type=dir;size=512;modify=20150115041252;create=20150115041212;perm=cdeflmp; .",
                "type=dir;size=512;modify=20150115041252;create=20150115041212;perm=cdeflmp; ..",
                "type=dir;size=512;modify=20150115041245;create=20150115041242;perm=cdeflmp; AVID",
                "type=dir;size=512;modify=20150115041252;create=20150115041250;perm=cdeflmp; QTS"
        };
        final AttributedList<Path> children = new FTPMlsdListResponseReader()
                .read(directory, Arrays.asList(replies));
        assertEquals(2, children.size());
    }
}
