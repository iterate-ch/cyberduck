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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.ftp.parser.LaxUnixFTPEntryParser;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class FTPStatListServiceTest {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final ListService service = new FTPStatListService(session,
                new CompositeFileEntryParser(Collections.singletonList(new UnixFTPEntryParser())));
        final Path directory = new FTPWorkdirService(session).find();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(directory, "test", EnumSet.of(Path.Type.file))));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(directory, "test", EnumSet.of(Path.Type.file))).attributes().getPermission());
        session.close();
    }

    @Test
    public void testParse() throws Exception {
        final List<String> list = new FTPStatListService(null, null).parse(
                212, new String[]{" drwxr-xr-x  11 root     root          8192 Dec 14 23:44 DK_BookStore"});
        assertEquals(1, list.size());
    }

    @Test
    public void testParse8006() throws Exception {
        final List<String> lines = Arrays.asList(
                "212-Status of /cgi-bin:",
                " drwxr-xr-x   3 1564466  15000           4 Jan 19 19:56 .",
                " drwxr-x---  13 1564466  15000          44 Jun 13 18:36 ..",
                " drwxr-xr-x   2 1564466  15000           2 May 25  2009 tmp",
                " End of status",
                "212 -rw-r--r--   1 1564466  15000        9859 Jan 19 19:56 adoptees.php");
        final FTPFileEntryParser parser = new UnixFTPEntryParser();
        final List<String> list = new FTPStatListService(null, parser).parse(
                212, lines.toArray(new String[lines.size()]));
        assertEquals(6, list.size());
        final Path parent = new Path("/cgi-bin", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> parsed = new FTPListResponseReader(parser, true).read(
                parent, list, new DisabledListProgressListener()
        );
        assertEquals(2, parsed.size());
        assertTrue(parsed.contains(new Path(parent, "tmp", EnumSet.of(Path.Type.directory))));
        assertTrue(parsed.contains(new Path(parent, "adoptees.php", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testParseEgnyte() throws Exception {
        final List<String> lines = Arrays.asList(
                "200-drwx------   0 - -            0 Jun 17 07:59 core",
                "200 -rw-------   0 David-Kocher -          529 Jun 17 07:59 App.config");
        final FTPFileEntryParser parser = new LaxUnixFTPEntryParser();
        final List<String> list = new FTPStatListService(null, parser).parse(
                200, lines.toArray(new String[lines.size()]));
        assertEquals(2, list.size());
        assertTrue(list.contains("drwx------   0 - -            0 Jun 17 07:59 core"));
        assertTrue(list.contains("-rw-------   0 David-Kocher -          529 Jun 17 07:59 App.config"));
        final Path parent = new Path("/cyberduck", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> parsed = new FTPListResponseReader(parser, true).read(
                parent, list, new DisabledListProgressListener());
        assertEquals(2, parsed.size());
    }

    @Test
    public void testParse9399() throws Exception {
        final List<String> list = new FTPStatListService(null, null).parse(
                212, new String[]{
                        "drwxrwxr-x   11 995      993          4096 Jan 11 21:24 .",
                        "drwxrwxr-x    4 995      993          4096 Jan 11 21:20 ..",
                        "drwxrwxr-x    2 995      993          4096 Jun 25  2015 assets",
                        "drwxrwxr-x    3 995      993          4096 Jan 11 18:05 css",
                        "drwxrwxr-x    2 995      993          4096 Jun 25  2015 fonts",
                        "drwxrwxr-x    8 995      993         12288 Dec 07 18:11 images",
                        "drwxrwxr-x    3 995      993          4096 Jun 25  2015 layerednavigationajax",
                        "lrwxrwxrwx    1 995      993            55 Jan 25 16:39 locale -> ../../../../app/design/frontend/liberty/liberty/locale/",
                        "drwxrwxr-x    5 995      993          4096 Jun 25  2015 magentothem",
                        "drwxrwxr-x    4 995      993          4096 Jun 25  2015 magentothem_blog",
                        "drwxrwxr-x    5 995      993          4096 Jun 25  2015 onepagecheckout",
                        "drwxrwxr-x    3 995      993          4096 Jul 24  2015 tm"

                });
        assertEquals(12, list.size());
    }
}
