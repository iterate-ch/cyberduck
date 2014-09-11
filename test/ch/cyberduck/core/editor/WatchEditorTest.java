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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.ui.AbstractController;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class WatchEditorTest extends AbstractTestCase {

    @Test
    public void testTemporaryPath() throws Exception {
        final Path path = new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file));
        path.attributes().setDuplicate(true);
        path.attributes().setVersionId("1");
        final WatchEditor editor = new WatchEditor(new AbstractController() {
            @Override
            public <T> Future<T> background(final BackgroundAction<T> runnable) {
                return null;
            }

            @Override
            public void invoke(final MainAction runnable) {
                //
            }

            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }
        }, new FTPSession(new Host("h")), new Application("com.apple.TextEdit", null), path);
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
        final WatchEditor editor = new WatchEditor(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }
        }, new FTPSession(new Host("h")), new Application("com.apple.TextEdit", null), file);
        assertEquals(new Path("/f1/f2/t.txt", EnumSet.of(Path.Type.file)), editor.getRemote());
    }
}