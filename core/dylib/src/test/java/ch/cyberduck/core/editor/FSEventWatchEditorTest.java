package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.pool.SingleSessionPool;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class FSEventWatchEditorTest {

    @Test
    public void testTemporaryPath() throws Exception {
        final Path path = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        path.attributes().setDuplicate(true);
        path.attributes().setVersionId("1");
        final FSEventWatchEditor editor = new FSEventWatchEditor(new Application("com.apple.TextEdit", null),
                new SingleSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "h")), PathCache.empty()), path, new DisabledListProgressListener());
        assertEquals(new Application("com.apple.TextEdit", null), editor.getApplication());
        assertEquals("t.txt", editor.getRemote().getName());
        final Local local = editor.getLocal();
        assertEquals("t.txt", local.getName());
        assertEquals("1", local.getParent().getName());
        assertEquals("f2", local.getParent().getParent().getName());
        assertEquals("f1", local.getParent().getParent().getParent().getName());
    }

    @Test
    public void testSymlinkTarget() throws Exception {
        final Path file = new Path("/f1/f2/s.txt", EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        file.setSymlinkTarget(new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file)));
        final FSEventWatchEditor editor = new FSEventWatchEditor(new Application("com.apple.TextEdit", null),
                new SingleSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "h")), PathCache.empty()), file, new DisabledListProgressListener());
        assertEquals(new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file)), editor.getRemote());
    }
}