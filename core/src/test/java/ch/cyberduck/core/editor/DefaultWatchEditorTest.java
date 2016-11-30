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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
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
import ch.cyberduck.core.pool.SingleSessionPool;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.EnumSet;
import java.util.UUID;

public class DefaultWatchEditorTest {

    @Test(expected = NoSuchFileException.class)
    public void testNotfound() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(null, new SingleSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol())), PathCache.empty(), new DisabledPasswordStore(), new DisabledLoginCallback()),
                new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        editor.watch(new Local(System.getProperty("java.io.tmpdir") + "/notfound", UUID.randomUUID().toString()), new DisabledFileWatcherListener());
    }

    @Test(expected = IOException.class)
    public void testEditNullApplicationNoFile() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(Application.notfound, new SingleSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol())), PathCache.empty(), new DisabledPasswordStore(), new DisabledLoginCallback()),
                new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        editor.edit(new DisabledApplicationQuitCallback(), new DisabledFileWatcherListener());
    }

    @Test(expected = IOException.class)
    public void testEditNullApplication() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(Application.notfound, new SingleSessionPool(new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol())), PathCache.empty(), new DisabledPasswordStore(), new DisabledLoginCallback()),
                new Path("/remote.txt", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        new DefaultLocalTouchFeature().touch(editor.getLocal());
        editor.edit(new DisabledApplicationQuitCallback(), new DisabledFileWatcherListener());
    }
}