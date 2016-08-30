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
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

public class TerminalListProgressListenerTest  {

    @Test
    public void testChunkEmpty() throws Exception {
        final TerminalListProgressListener l = new TerminalListProgressListener();
        l.chunk(new Path("/folder", EnumSet.of(Path.Type.directory)), AttributedList.<Path>emptyList());
    }

    @Test
    public void testChunk() throws Exception {
        final TerminalListProgressListener l = new TerminalListProgressListener();
        l.chunk(new Path("/folder", EnumSet.of(Path.Type.directory)), new AttributedList<Path>(Collections.singletonList(new Path("/folder/f", EnumSet.of(Path.Type.file)))));
    }
}