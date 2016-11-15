package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class TerminalTransferFactoryTest  {

    @Test
    public void testCreate() throws Exception {
        final CommandLineParser parser = new PosixParser();

        final Transfer transfer = new TerminalTransferFactory().create(parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote"}),
                new Host(new SwiftProtocol()), new Path("/remote", EnumSet.of(Path.Type.directory)), Collections.<TransferItem>emptyList());
        assertEquals(Transfer.Type.download, transfer.getType());
    }

    @Test
    public void testFilter() throws Exception {
        final CommandLineParser parser = new PosixParser();

        final Transfer transfer = new TerminalTransferFactory().create(parser.parse(TerminalOptionsBuilder.options(), new String[]{"--download", "rackspace://cdn.cyberduck.ch/remote/*.css"}),
                new Host(new SwiftProtocol()), new Path("/remote/*.css", EnumSet.of(Path.Type.directory)), Collections.<TransferItem>emptyList());
        assertEquals(Transfer.Type.download, transfer.getType());
        final PathCache cache = new PathCache(1);
        transfer.withCache(cache);
        cache.clear();
        cache.put(new Path("/remote", EnumSet.of(Path.Type.directory)), new AttributedList<Path>(Collections.singletonList(new Path("/remote/file.css", EnumSet.of(Path.Type.file)))));
        assertFalse(transfer.list(null, new Path("/remote", EnumSet.of(Path.Type.directory)), new Local("/tmp"), new DisabledListProgressListener()).isEmpty());
        cache.clear();
        cache.put(new Path("/remote", EnumSet.of(Path.Type.directory)), new AttributedList<Path>(Collections.singletonList(new Path("/remote/file.png", EnumSet.of(Path.Type.file)))));
        assertTrue(transfer.list(null, new Path("/remote", EnumSet.of(Path.Type.directory)), new Local("/tmp"), new DisabledListProgressListener()).isEmpty());
    }
}