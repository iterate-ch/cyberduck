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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.serializer.TransferDictionary;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SyncTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new SyncTransfer(new Host("t"),
                new TransferItem(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("/", "t")));
        t.addSize(4L);
        t.addTransferred(3L);
        final Transfer serialized = new TransferDictionary().deserialize(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testAction() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host("t"), new TransferItem(p, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        }));
        final AtomicBoolean prompt = new AtomicBoolean();
        assertEquals(null, t.action(new NullSession(new Host("t")), false, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt() {
                prompt.set(true);
                return null;
            }
        }));
        assertTrue(prompt.get());
    }

    @Test
    public void testFilterDownload() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host("t"), new TransferItem(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
        final TransferPathFilter filter = t.filter(new NullSession(new Host("t")), TransferAction.download, new DisabledProgressListener());
        final Path test = new Path(p, "a", EnumSet.of(Path.Type.file));
        assertFalse(filter.accept(test, new NullLocal(System.getProperty("java.io.tmpdir"), "a"),
                new TransferStatus().exists(true)));
    }

    @Test
    public void testFilterUpload() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host("t"), new TransferItem(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
        final TransferPathFilter filter = t.filter(new NullSession(new Host("t")), TransferAction.upload, new DisabledProgressListener());
        final Path test = new Path(p, "a", EnumSet.of(Path.Type.file));
        assertTrue(filter.accept(test, new NullLocal(System.getProperty("java.io.tmpdir"), "a"),
                new TransferStatus().exists(true)));
    }

    @Test
    public void testFilterMirror() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        Transfer t = new SyncTransfer(new Host("t"), new TransferItem(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
        final TransferPathFilter filter = t.filter(new NullSession(new Host("t")), TransferAction.mirror, new DisabledProgressListener());
        final Path test = new Path(p, "a", EnumSet.of(Path.Type.file));
        assertTrue(filter.accept(test, new NullLocal(System.getProperty("java.io.tmpdir"), "a"),
                new TransferStatus().exists(true)));
    }

    @Test
    public void testChildrenDuplicate() throws Exception {
        final NullLocal local = new NullLocal(System.getProperty("java.io.tmpdir") + "t", "a");
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                final AttributedList<Local> list = new AttributedList<Local>();
                list.add(local);
                return list;
            }
        };
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Path remote = new Path(root, "a", EnumSet.of(Path.Type.file));
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> list = new AttributedList<Path>();
                if(file.equals(root.getParent())) {
                    list.add(root);
                }
                else {
                    list.add(remote);
                }
                return list;
            }
        };
        directory.mkdir();
        Transfer t = new SyncTransfer(new Host("t"), new TransferItem(root, directory));
        final List<TransferItem> list = t.list(session, root, directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
    }

    @Test
    public void testChildrenLocalOnly() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        };
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                final AttributedList<Local> list = new AttributedList<Local>();
                list.add(new NullLocal(System.getProperty("java.io.tmpdir") + "/t", "a"));
                return list;
            }
        };
        directory.mkdir();
        Transfer t = new SyncTransfer(new Host("t"), new TransferItem(root, directory));
        final List<TransferItem> list = t.list(session, root, directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
        final NullLocal local = new NullLocal(System.getProperty("java.io.tmpdir"), "a");
        assertFalse(t.filter(session, TransferAction.download, new DisabledProgressListener()).accept(root, local, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.upload, new DisabledProgressListener()).accept(root, local, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.mirror, new DisabledProgressListener()).accept(root, local, new TransferStatus().exists(true)));
    }

    @Test
    public void testChildrenRemoteOnly() throws Exception {
        final Path root = new Path("t", EnumSet.of(Path.Type.directory));
        final Path a = new Path(root, "a", EnumSet.of(Path.Type.file));
        final NullLocal directory = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>();
            }
        };
        directory.mkdir();
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> list = new AttributedList<Path>();
                if(file.equals(root.getParent())) {
                    list.add(root);
                }
                else {
                    list.add(a);
                }
                return list;
            }
        };
        final Transfer t = new SyncTransfer(new Host("t"), new TransferItem(root, directory));
        final List<TransferItem> list = t.list(session, root, directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
        final NullLocal local = new NullLocal(directory, "a") {
            @Override
            public boolean exists() {
                return false;
            }
        };
        assertTrue(t.filter(session, TransferAction.download, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.upload, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.mirror, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.download, new DisabledProgressListener()).accept(a, local, new TransferStatus().exists(true)));
        // Because root is directory
        assertTrue(t.filter(session, TransferAction.upload, new DisabledProgressListener()).accept(root, directory, new TransferStatus().exists(true)));
        assertFalse(t.filter(session, TransferAction.upload, new DisabledProgressListener()).accept(a, local, new TransferStatus().exists(true)));
    }
}
