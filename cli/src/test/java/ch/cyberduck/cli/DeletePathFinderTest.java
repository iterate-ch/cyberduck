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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class DeletePathFinderTest  {

    @Test
    public void testFindDirectory() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--delete", "rackspace://cdn.cyberduck.ch/remote"});

        assertTrue(new DeletePathFinder().find(input, TerminalAction.delete, new Path("/remote", EnumSet.of(Path.Type.directory))).contains(
                new TransferItem(new Path("/remote", EnumSet.of(Path.Type.directory)))
        ));
    }

    @Test
    public void testFindFile() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--delete", "rackspace://cdn.cyberduck.ch/remote"});

        assertTrue(new DeletePathFinder().find(input, TerminalAction.delete, new Path("/remote", EnumSet.of(Path.Type.file))).contains(
                new TransferItem(new Path("/remote", EnumSet.of(Path.Type.file)))
        ));
    }

    @Test
    public void testFindWildcard() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(TerminalOptionsBuilder.options(), new String[]{"--delete", "rackspace://cdn.cyberduck.ch/remote/*.txt"});

        assertTrue(new DeletePathFinder().find(input, TerminalAction.delete, new Path("/remote/*.txt", EnumSet.of(Path.Type.file))).contains(
                new TransferItem(new Path("/remote", EnumSet.of(Path.Type.directory)))
        ));
    }
}