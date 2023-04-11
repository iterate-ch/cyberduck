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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.NullTransferSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledFileWatcherListener;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractEditorTest {

    @Test
    public void testOpen() throws Exception {
        final AtomicBoolean t = new AtomicBoolean();
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullTransferSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type.equals(Read.class)) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) {
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
                return super._getFeature(type);
            }
        };
        final AtomicBoolean e = new AtomicBoolean();
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        file.attributes().setSize("content".getBytes().length);
        final AbstractEditor editor = new AbstractEditor(host, file, new DisabledProgressListener()) {
            @Override
            public void close() {
                //
            }

            @Override
            protected void edit(final Application editor, final Path file, final Local temporary, final FileWatcherListener listener, final ApplicationQuitCallback quit) {
                e.set(true);
            }

            @Override
            protected void watch(final Application application, final Local temporary, final FileWatcherListener listener, final ApplicationQuitCallback quit) {
                //
            }
        };
        editor.open(new Application("com.editor"), new DisabledApplicationQuitCallback(), new DisabledFileWatcherListener()).run(session);
        assertTrue(t.get());
        assertTrue(e.get());
    }
}
