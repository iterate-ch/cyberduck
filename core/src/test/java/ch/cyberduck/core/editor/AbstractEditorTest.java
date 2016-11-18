package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledFileWatcherListener;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class AbstractEditorTest {

    @Test
    public void testEquals() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        assertEquals(
                new DisabledEditor(new Application("i"), new SingleSessionPool(null, session, PathCache.empty()), new Path("/p/f", EnumSet.of(Path.Type.file))),
                new DisabledEditor(new Application("i"), new SingleSessionPool(null, session, PathCache.empty()), new Path("/p/f", EnumSet.of(Path.Type.file)))
        );
        assertNotEquals(
                new DisabledEditor(new Application("i"), new SingleSessionPool(null, session, PathCache.empty()), new Path("/p/f", EnumSet.of(Path.Type.file))),
                new DisabledEditor(new Application("i"), new SingleSessionPool(null, session, PathCache.empty()), new Path("/p/g", EnumSet.of(Path.Type.file)))
        );
        assertNotEquals(
                new DisabledEditor(new Application("a"), new SingleSessionPool(null, session, PathCache.empty()), new Path("/p/f", EnumSet.of(Path.Type.file))),
                new DisabledEditor(new Application("i"), new SingleSessionPool(null, session, PathCache.empty()), new Path("/p/f", EnumSet.of(Path.Type.file)))
        );
    }

    @Test
    public void testOpen() throws Exception {
        final AtomicBoolean t = new AtomicBoolean();
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Read.class)) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
                            t.set(true);
                            return IOUtils.toInputStream("content", Charset.defaultCharset());
                        }

                        @Override
                        public boolean offset(final Path file) {
                            assertEquals(new Path("/f", EnumSet.of(Path.Type.file)), file);
                            return false;
                        }
                    };
                }
                return super.getFeature(type);
            }
        };
        final AtomicBoolean e = new AtomicBoolean();
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        file.attributes().setSize("content".getBytes().length);
        final AbstractEditor editor = new AbstractEditor(new Application("com.editor"), new SingleSessionPool(null, session, PathCache.empty()), file, new DisabledProgressListener()) {
            @Override
            protected void edit(final ApplicationQuitCallback quit, final FileWatcherListener listener) throws IOException {
                e.set(true);
            }

            @Override
            protected void watch(final Local local, final FileWatcherListener listener) throws IOException {
                //
            }
        };
        editor.open(new DisabledApplicationQuitCallback(), new DisabledTransferErrorCallback(), new DisabledFileWatcherListener()).run(session);
        assertTrue(t.get());
        assertNotNull(editor.getLocal());
        assertTrue(e.get());
        assertTrue(editor.getLocal().exists());
    }

    private class DisabledEditor extends AbstractEditor {
        public DisabledEditor(final Application application, final SessionPool session, final Path file) {
            super(application, session, file, new DisabledProgressListener());
        }

        @Override
        protected void watch(final Local local, final FileWatcherListener listener) throws IOException {
            //
        }
    }
}
