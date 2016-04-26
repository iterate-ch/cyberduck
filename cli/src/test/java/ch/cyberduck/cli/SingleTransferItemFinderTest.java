package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

public class SingleTransferItemFinderTest  {

    @Test
    public void testNoLocalInOptionsDownload() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote"});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.download, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        assertEquals(new TransferItem(new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)), LocalFactory.get(System.getProperty("user.dir") + "/remote")),
                found.iterator().next());
    }

    @Test
    public void testLocalInOptionsDownload() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final String temp = System.getProperty("java.io.tmpdir");
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote", String.format("%s/f", temp)});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.download, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        assertEquals(new TransferItem(new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)), LocalFactory.get(String.format("%s/f", temp))),
                found.iterator().next());

    }

    @Test
    public void testNoLocalInOptionsUploadFile() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "rackspace://cdn.cyberduck.ch/remote"});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.upload, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertTrue(found.isEmpty());
    }

    @Test
    public void testDeferUploadNameFromLocal() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final String temp = System.getProperty("java.io.tmpdir");
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "ftps://test.cyberduck.ch/remote/", String.format("%s/f", temp)});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.upload, new Path("/remote/", EnumSet.of(Path.Type.directory)));
        assertFalse(found.isEmpty());
        assertEquals(new TransferItem(new Path("/remote/f", EnumSet.of(Path.Type.file)), LocalFactory.get(String.format("%s/f", temp))),
                found.iterator().next());
    }

    @Test
    public void testUploadDirectory() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "ftps://test.cyberduck.ch/remote/", System.getProperty("java.io.tmpdir")});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.upload, new Path("/remote/", EnumSet.of(Path.Type.directory)));
        assertFalse(found.isEmpty());
        final Iterator<TransferItem> iter = found.iterator();
        final Local temp = LocalFactory.get(System.getProperty("java.io.tmpdir"));
        assertTrue(temp.getType().contains(Path.Type.directory));
        assertEquals(new TransferItem(new Path("/remote", EnumSet.of(Path.Type.directory)), temp), iter.next());
    }

    @Test
    public void testUploadDirectoryServerRoot() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "ftps://test.cyberduck.ch/", System.getProperty("java.io.tmpdir")});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.upload, new Path("/", EnumSet.of(Path.Type.directory)));
        assertFalse(found.isEmpty());
        final Iterator<TransferItem> iter = found.iterator();
        final Local temp = LocalFactory.get(System.getProperty("java.io.tmpdir"));
        assertTrue(temp.getType().contains(Path.Type.directory));
        assertEquals(new TransferItem(new Path("/", EnumSet.of(Path.Type.directory)), temp), iter.next());
    }

    @Test
    public void testDownloadFileToDirectoryTarget() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final String temp = System.getProperty("java.io.tmpdir");
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "ftps://test.cyberduck.ch/remote/f", temp});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.download, new Path("/remote/f", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        final Iterator<TransferItem> iter = found.iterator();
        assertEquals(new TransferItem(new Path("/remote/f", EnumSet.of(Path.Type.file)), LocalFactory.get(String.format("%s/f", temp))),
                iter.next());
    }

    @Test
    public void testDownloadDirectoryTarget() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final String temp = System.getProperty("java.io.tmpdir");
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "ftps://test.cyberduck.ch/remote/", temp});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.download, new Path("/remote/", EnumSet.of(Path.Type.directory)));
        assertFalse(found.isEmpty());
        final Iterator<TransferItem> iter = found.iterator();
        assertEquals(new TransferItem(new Path("/remote", EnumSet.of(Path.Type.directory)), LocalFactory.get(String.format("%s/remote", temp))),
                iter.next());
    }
}