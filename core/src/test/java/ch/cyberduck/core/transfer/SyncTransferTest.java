package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.NullTransferSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.synchronization.Comparison;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SyncTransferTest {

    @Test
    public void testAction() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(p, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<>(Collections.singletonList(new NullLocal("p", "a")));
            }
        }));
        final AtomicBoolean prompt = new AtomicBoolean();
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        assertNull(t.action(session, null, false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                prompt.set(true);
                return null;
            }
        }, new DisabledListProgressListener()));
        assertTrue(prompt.get());
    }

    @Test
    public void testFilterDownload() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        };
        final TransferPathFilter filter = t.filter(session, null, TransferAction.download, new DisabledProgressListener());
        final Path test = new Path(p, "a", EnumSet.of(Path.Type.file));
        assertFalse(filter.accept(test, new NullLocal(System.getProperty("java.io.tmpdir"), "a"),
                new TransferStatus().exists(true), new DisabledProgressListener()));
    }

    @Test
    public void testFilterUpload() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final TransferPathFilter filter = t.filter(session, null, TransferAction.upload, new DisabledProgressListener());
        final Path test = new Path(p, "a", EnumSet.of(Path.Type.file));
        assertTrue(filter.accept(test, new NullLocal(System.getProperty("java.io.tmpdir"), "a") {
                @Override
                public boolean exists() {
                    return true;
                }

                @Override
                public LocalAttributes attributes() {
                    return new LocalAttributes(this.getAbsolute()) {
                        @Override
                        public long getSize() {
                            return 1L;
                        }
                    };
                }
            },
                new TransferStatus().exists(true), new DisabledProgressListener()));
    }

    @Test
    public void testFilterMirror() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        final Path a = new Path(p, "a", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withSize(2L));
        final Path b = new Path(p, "b", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withSize(1L));
        final PathCache cache = new PathCache(1);
        cache.put(p, new AttributedList<>(Arrays.asList(a, b)));
        SyncTransfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
        t.withCache(cache);
        final NullSession session = new NullTransferSession(new Host(new TestProtocol()));
        final TransferPathFilter filter = t.filter(session, null, TransferAction.mirror, new DisabledProgressListener());
        assertTrue(filter.accept(a, new NullLocal(System.getProperty("java.io.tmpdir"), "a") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes(this.getAbsolute()) {
                    @Override
                    public long getSize() {
                        return 1L;
                    }
                };
            }
        }, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertTrue(filter.accept(b, new NullLocal(System.getProperty("java.io.tmpdir"), "b") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes(this.getAbsolute()) {
                    @Override
                    public long getSize() {
                        return 0L;
                    }
                };
            }
        }, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertEquals(Comparison.local, t.compare(new TransferItem(a, new NullLocal(System.getProperty("java.io.tmpdir"), "a"))));
        assertEquals(Comparison.remote, t.compare(new TransferItem(b, new NullLocal(System.getProperty("java.io.tmpdir"), "b"))));
    }

    @Test
    public void testChildrenRemoteAndLocalExist() throws Exception {
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                final AttributedList<Local> list = new AttributedList<>();
                list.add(new NullLocal(this, "a"));
                return list;
            }
        };
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Path remote = new Path(root, "a", EnumSet.of(Path.Type.file));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> list = new AttributedList<>();
                if(file.equals(root.getParent())) {
                    list.add(root);
                }
                else {
                    list.add(remote);
                }
                return list;
            }
        };
        new DefaultLocalDirectoryFeature().mkdir(directory);
        Transfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(root, directory));
        final List<TransferItem> list = t.list(session, root, directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
    }

    @Test
    public void testChildrenLocalOnly() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        };
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                final AttributedList<Local> list = new AttributedList<>();
                list.add(new NullLocal(System.getProperty("java.io.tmpdir") + "/t", "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                });
                return list;
            }
        };
        new DefaultLocalDirectoryFeature().mkdir(directory);
        Transfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(root, directory));
        final List<TransferItem> list = t.list(session, root, directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
        final NullLocal local = new NullLocal(System.getProperty("java.io.tmpdir"), "a") {
            @Override
            public boolean exists() {
                return true;
            }
        };
        assertFalse(t.filter(session, null, TransferAction.download, new DisabledProgressListener()).accept(root, local, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertTrue(t.filter(session, null, TransferAction.upload, new DisabledProgressListener()).accept(root, local, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertTrue(t.filter(session, null, TransferAction.mirror, new DisabledProgressListener()).accept(root, local, new TransferStatus().exists(true), new DisabledProgressListener()));
    }

    @Test
    public void testChildrenRemoteOnly() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Path a = new Path(root, "a", EnumSet.of(Path.Type.file));
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                return new AttributedList<>();
            }
        };
        new DefaultLocalDirectoryFeature().mkdir(directory);
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> list = new AttributedList<>();
                if(file.equals(root.getParent())) {
                    list.add(root);
                }
                else {
                    list.add(a);
                }
                return list;
            }
        };
        final Transfer t = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(root, directory));
        final List<TransferItem> list = t.list(session, root, directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
        final NullLocal local = new NullLocal(directory, "a") {
            @Override
            public boolean exists() {
                return false;
            }
        };
        assertTrue(t.filter(session, null, TransferAction.download, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertTrue(t.filter(session, null, TransferAction.upload, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertTrue(t.filter(session, null, TransferAction.mirror, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertTrue(t.filter(session, null, TransferAction.download, new DisabledProgressListener()).accept(a, local, new TransferStatus().exists(true), new DisabledProgressListener()));
        // Because root is directory
        assertTrue(t.filter(session, null, TransferAction.upload, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true), new DisabledProgressListener()));
        assertFalse(t.filter(session, null, TransferAction.upload, new DisabledProgressListener()).accept(a, local, new TransferStatus().exists(true), new DisabledProgressListener()));
    }
}
