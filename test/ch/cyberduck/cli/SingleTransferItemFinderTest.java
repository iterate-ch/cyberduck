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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

public class SingleTransferItemFinderTest extends AbstractTestCase{

    @Test
    public void testNoLocalInOptionsDownload() throws Exception {
        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote"});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.download, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        assertEquals(new TransferItem(new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)), LocalFactory.get("remote")),
                found.iterator().next());

    }

    @Test
    public void testLocalInOptionsDownload() throws Exception {
        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote", "/tmp/f"});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.download, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertFalse(found.isEmpty());
        assertEquals(new TransferItem(new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)), LocalFactory.get("/tmp/f")),
                found.iterator().next());

    }

    @Test
    public void testNoLocalInOptionsUploadFile() throws Exception {
        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--upload", "rackspace://cdn.cyberduck.ch/remote"});

        final Set<TransferItem> found = new SingleTransferItemFinder().find(input, TerminalAction.upload, new Path("/cdn.cyberduck.ch/remote", EnumSet.of(Path.Type.file)));
        assertTrue(found.isEmpty());

    }
}