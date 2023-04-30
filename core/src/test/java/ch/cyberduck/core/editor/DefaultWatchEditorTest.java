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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.local.DisabledApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledFileWatcherListener;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.EnumSet;
import java.util.UUID;

public class DefaultWatchEditorTest {

    @Test(expected = NoSuchFileException.class)
    public void testNotfound() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(new Host(new TestProtocol()), new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        editor.watch(new Application("com.app"), new Local(System.getProperty("java.io.tmpdir") + "/notfound", UUID.randomUUID().toString()), new DisabledFileWatcherListener(),
                new DisabledApplicationQuitCallback());
    }

    @Test(expected = IOException.class)
    public void testEditNullApplicationNoFile() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(new Host(new TestProtocol()), new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        final Path file = new Path("/remote", EnumSet.of(Path.Type.file));
        editor.edit(EditorFactory.getEditor(file.getName()), file, new DefaultTemporaryFileService().create("remote"), new DisabledFileWatcherListener(), new DisabledApplicationQuitCallback());
    }

    @Test(expected = IOException.class)
    public void testEditNullApplication() throws Exception {
        final DefaultWatchEditor editor = new DefaultWatchEditor(new Host(new TestProtocol()), new Path("/remote", EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
        final Local local = new DefaultTemporaryFileService().create("remote");
        new DefaultLocalTouchFeature().touch(local);
        final Path file = new Path("/remote.txt", EnumSet.of(Path.Type.file));
        editor.edit(EditorFactory.getEditor(file.getName()), file, local, new DisabledFileWatcherListener(), new DisabledApplicationQuitCallback());
    }
}
