package ch.cyberduck.core.editor;

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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.local.DisabledApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledFileWatcherListener;
import ch.cyberduck.core.pool.StatelessSessionPool;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DefaultWatchEditorTest {

    @Test(expected = NoSuchFileException.class)
    public void testNotfound() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(null, new StatelessSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol())), PathCache.empty(), new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())),
            new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        editor.watch(new Local(System.getProperty("java.io.tmpdir") + "/notfound", UUID.randomUUID().toString()), new DisabledFileWatcherListener());
    }

    @Test(expected = IOException.class)
    public void testEditNullApplicationNoFile() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(Application.notfound, new StatelessSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol())), PathCache.empty(), new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())),
                new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        editor.edit(new DisabledApplicationQuitCallback(), new DisabledFileWatcherListener());
    }

    @Test(expected = IOException.class)
    public void testEditNullApplication() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(Application.notfound, new StatelessSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol())), PathCache.empty(), new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())),
            new Path("/remote.txt", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        new DefaultLocalTouchFeature().touch(editor.getLocal());
        editor.edit(new DisabledApplicationQuitCallback(), new DisabledFileWatcherListener());
    }

    @Test
    public void testTemporaryPath() {
        final Path path = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        path.attributes().setDuplicate(true);
        path.attributes().setVersionId("1");
        final DefaultWatchEditor editor = new DefaultWatchEditor(new Application("com.apple.TextEdit", null),
            new StatelessSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "h")), PathCache.empty(),
                new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())), path, new DisabledListProgressListener());
        assertEquals(new Application("com.apple.TextEdit", null), editor.getApplication());
        assertEquals("t.txt", editor.getRemote().getName());
        final Local local = editor.getLocal();
        assertEquals("t.txt", local.getName());
        assertEquals("1", local.getParent().getName());
        assertEquals("f2", local.getParent().getParent().getName());
        assertEquals("f1", local.getParent().getParent().getParent().getName());
    }

    @Test
    public void testSymlinkTarget() {
        final Path file = new Path("/f1/f2/s.txt", EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        file.setSymlinkTarget(new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file)));
        final DefaultWatchEditor editor = new DefaultWatchEditor(new Application("com.apple.TextEdit", null),
            new StatelessSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "h")), PathCache.empty(),
                new DisabledTranscriptListener(), new DefaultVaultRegistry(new DisabledPasswordCallback())), file, new DisabledListProgressListener());
        assertEquals(new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file)), editor.getRemote());
    }
}
