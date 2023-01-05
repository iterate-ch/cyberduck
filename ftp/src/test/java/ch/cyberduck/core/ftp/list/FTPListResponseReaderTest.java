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
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.ftp.FTPParserSelector;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPListResponseReaderTest {

    @Test(expected = FTPInvalidListException.class)
    public void test3243() throws Exception {
        Path path = new Path("/SunnyD", EnumSet.of(Path.Type.directory));
        assertEquals("SunnyD", path.getName());
        assertEquals("/SunnyD", path.getAbsolute());
        final AttributedList<Path> list = new AttributedList<Path>();
        new FTPListResponseReader(new FTPParserSelector().getParser("UNIX")).read(path, Collections.singletonList(
                " drwxrwx--x 1 owner group          512 Jun 12 15:40 SunnyD")
        );
    }

    @Test
    public void testParseSymbolicLink() throws Exception {
        Path path = new Path("/", EnumSet.of(Path.Type.directory));
        assertEquals("/", path.getName());
        assertEquals("/", path.getAbsolute());

        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"))
            .read(path, Collections.singletonList(
                    "lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 www -> /www/basic/mk")
            );

        assertFalse(list.isEmpty());
        final Path parsed = list.get(0);
        assertTrue(parsed.isSymbolicLink());
        assertEquals("/www/basic/mk", parsed.getSymlinkTarget().getAbsolute());
        assertEquals(new Permission("rwxrwxrwx"), parsed.attributes().getPermission());
    }

    @Test(expected = FTPInvalidListException.class)
    public void test3763() throws Exception {
        Path path = new Path("/www", EnumSet.of(Path.Type.directory));
        assertEquals("www", path.getName());
        assertEquals("/www", path.getAbsolute());
        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"), true)
            .read(path, Collections.singletonList(
                    "lrwxrwxrwx    1 mk basicgrp       27 Sep 23  2004 /home/mk/www -> /www/basic/mk")
            );
    }

    @Test
    public void testStickyBit() throws Exception {
        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"))
            .read(new Path("/", EnumSet.of(Path.Type.directory)),
                    Collections.singletonList("-rwsrwSr-T 1 dkocher dkocher         0 Sep  6 22:27 t")
            );
        final Path parsed = list.get(new Path("/t", EnumSet.of(Path.Type.file)));
        assertNotNull(parsed);
        assertTrue(parsed.attributes().getPermission().isSticky());
        assertTrue(parsed.attributes().getPermission().isSetuid());
        assertTrue(parsed.attributes().getPermission().isSetgid());
        assertEquals(new Permission("rwsrwSr-T"), parsed.attributes().getPermission());
    }

    @Test
    @Ignore
    public void testParseHardlinkCountBadFormat() throws Exception {
        Path path = new Path(
            "/store/public/brain", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "drwx------+111 mi       public       198 Dec 17 12:29 unsorted"
        };

        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"))
                .read(path, Arrays.asList(replies));
        assertEquals(1, list.size());
        final Path parsed = list.get(0);
        assertEquals("unsorted", parsed.getName());
        assertEquals("/store/public/brain", parsed.getParent().getAbsolute());
    }

    @Test
    public void testParseAbsolutePaths() throws Exception {
        Path path = new Path(
            "/data/FTP_pub", EnumSet.of(Path.Type.directory));

        String[] replies = new String[]{
            "- [RWCEAFMS] Petersm                             0 May 05  2004 /data/FTP_pub/WelcomeTo_PeakFTP"
        };
        final CompositeFileEntryParser parser = new FTPParserSelector().getParser("NETWARE  Type : L8");
        final AttributedList<Path> list = new FTPListResponseReader(parser).read(path, Arrays.asList(replies)
        );
        assertEquals(1, list.size());
        final Path parsed = list.get(0);
        assertEquals("WelcomeTo_PeakFTP", parsed.getName());
        assertEquals("/data/FTP_pub", parsed.getParent().getAbsolute());
        assertFalse(parsed.attributes().getPermission().isSticky());
        assertFalse(parsed.attributes().getPermission().isSetuid());
        assertFalse(parsed.attributes().getPermission().isSetgid());
    }

    @Test(expected = ListCanceledException.class)
    @Ignore
    public void testLimit() throws Exception {
        final CompositeFileEntryParser parser = new FTPParserSelector().getParser("NETWARE  Type : L8");
        final AttributedList<Path> list = new FTPListResponseReader(parser).read(
            new Path("/", EnumSet.of(Path.Type.directory)), Collections.singletonList(
                        "lrwxrwxrwx    1 ftp      ftp            23 Feb 05 06:51 debian -> ../pool/4/mirror/debian")
        );
    }

    @Test
    public void testNoChunkNotification() throws Exception {
        final CompositeFileEntryParser parser = new FTPParserSelector().getParser("NETWARE  Type : L8");
        final AttributedList<Path> list = new FTPListResponseReader(parser).read(
            new Path("/", EnumSet.of(Path.Type.directory)), Collections.singletonList(
                        "lrwxrwxrwx    1 ftp      ftp            23 Feb 05 06:51 debian -> ../pool/4/mirror/debian")
        );
    }

    @Test(expected = FTPInvalidListException.class)
    public void testListNoRead() throws Exception {
        final Path directory = new Path("/sandbox/noread", EnumSet.of(Path.Type.directory));
        final String[] lines = new String[]{
            "213-Status follows:",
            "d-w--w----    2 1003     1003         4096 Nov 06  2013 noread",
            "213 End of status"};

        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"), true)
                .read(directory, Arrays.asList(lines));
        assertEquals(0, list.size());
    }

    @Test(expected = FTPInvalidListException.class)
    public void testListSymbolicLink() throws Exception {
        final Path directory = new Path("/home/barchouston/www", EnumSet.of(Path.Type.directory));
        final String[] lines = new String[]{
                "213-status of /home/barchouston/www:",
                "lrwxrwxrwx   1 barchous barchous       16 Apr  2  2002 /home/barchouston/www -> /www/barchouston",
                "213 End of Status"};
        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"), true)
                .read(directory, Arrays.asList(lines));
        assertEquals(0, list.size());
    }

    @Test
    public void testDirectoryWithinSameName() throws Exception {
        // #8577
        final Path directory = new Path("/aaa_bbb/untitled folder", EnumSet.of(Path.Type.directory));
        final String[] lines = new String[]{
                "drwx------   0 null null            0 Feb  4 21:40 untitled folder",
        };
        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"))
                .read(directory, Arrays.asList(lines));
        assertEquals(1, list.size());
        assertEquals("/aaa_bbb/untitled folder/untitled folder", list.get(0).getAbsolute());
    }

    @Test(expected = FTPInvalidListException.class)
    public void testDirectoryWithinSameNameLenient() throws Exception {
        // #8577
        final Path directory = new Path("/aaa_bbb/untitled folder", EnumSet.of(Path.Type.directory));
        final String[] lines = new String[]{
            "drwx------   0 null null            0 Feb  4 21:40 untitled folder",
        };
        new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"), true)
                .read(directory, Arrays.asList(lines));
    }

    @Test
    public void testParseSymbolicLinkWorkingDirectory() throws Exception {
        final List<String> lines = new FTPStatListService(null, null).parse(
                211, new String[]{
                        "211-Status of /:",
                        "211-lrwxrwxrwx   1 root     root            1 Jun 21 09:59 public_html -> ."
                });
        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX"))
                .read(new Path("/", EnumSet.of(Path.Type.directory)), lines);
        assertEquals(1, list.size());
        assertEquals("/public_html", list.get(0).getAbsolute());
        assertEquals("/public_html", list.get(0).getSymlinkTarget().getAbsolute());
    }

    @Test
    public void testParseMD1766() throws Exception {
        final List<String> lines = new FTPStatListService(null, null).parse(
                211, new String[]{
                        "lrwxrwxrwx 1 sss 7 Nov 2 2015 bin",
                        "lrwxrwxrwx 1 sss 6 Nov 2 2015 home1",
                        "lrwxrwxrwx 1 sss 15 Nov 2 2015 vvvdev"
//                        "lrwxrwxrwx 1 USER SSS 7 Nov 02 2015 bin -> script/",
//                        "lrwxrwxrwx 1 USER SSS 6 Nov 02 2015 home1 -> /home1",
//                        "lrwxrwxrwx 1 USER SSS 15 Nov 02 2015 vvvdev -> /fff/dev/vvvdev"
                });
        final AttributedList<Path> list = new FTPListResponseReader(new FTPParserSelector().getParser("UNIX Type: L8 Version: BSD-44"), true)
                .read(new Path("/", EnumSet.of(Path.Type.directory)), lines);
        assertEquals(3, list.size());
        assertNull(list.get(0).getSymlinkTarget());
        assertFalse(list.get(0).isSymbolicLink());
        assertNull(list.get(1).getSymlinkTarget());
        assertFalse(list.get(1).isSymbolicLink());
        assertNull(list.get(2).getSymlinkTarget());
        assertFalse(list.get(2).isSymbolicLink());
    }
}
