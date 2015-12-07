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

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

public class GlobTransferItemFinderTest  {

    @Test
    public void testNoLocalInOptionsDownload() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote"});

        final Set<TransferItem> found = new GlobTransferItemFinder().find(input, TerminalAction.download, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        assertEquals(new TransferItem(new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)), LocalFactory.get(System.getProperty("user.dir") + "/remote")),
                found.iterator().next());

    }

    @Test
    public void testNoLocalInOptionsUploadFile() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "rackspace://cdn.cyberduck.ch/remote"});

        final Set<TransferItem> found = new GlobTransferItemFinder().find(input, TerminalAction.upload, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFind() throws Exception {
        File.createTempFile("temp", ".duck");
        final File f = File.createTempFile("temp", ".duck");
        File.createTempFile("temp", ".false");

        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "rackspace://cdn.cyberduck.ch/remote", f.getParent() + "/*.duck"});

        final Set<TransferItem> found = new GlobTransferItemFinder().find(input, TerminalAction.upload, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        assertTrue(found.contains(new TransferItem(
                new Path(new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.directory)), f.getName(), EnumSet.of(Path.Type.file)),
                new Local(f.getAbsolutePath()))));
    }
}