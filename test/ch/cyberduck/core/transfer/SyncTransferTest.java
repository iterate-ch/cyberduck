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
import ch.cyberduck.core.DisabledListProgressListener;import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SyncTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new SyncTransfer(new Host("t"), new Path("t", Path.FILE_TYPE));
        t.addSize(4L);
        t.addTransferred(3L);
        final SyncTransfer serialized = new SyncTransfer(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
    }

    @Test
    public void testAction() throws Exception {
        final Path p = new Path("t", Path.DIRECTORY_TYPE);
        p.setLocal(new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        });
        Transfer t = new SyncTransfer(new Host("t"), p);
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
        final Path p = new Path("t", Path.DIRECTORY_TYPE);
        p.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
        Transfer t = new SyncTransfer(new Host("t"), p);
        final TransferPathFilter filter = t.filter(new NullSession(new Host("t")), TransferAction.download);
        final Path test = new Path(p, "a", Path.FILE_TYPE);
        test.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
        assertFalse(filter.accept(test, new TransferStatus().exists(true)));
    }

    @Test
    public void testFilterUpload() throws Exception {
        final Path p = new Path("t", Path.DIRECTORY_TYPE);
        p.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
        Transfer t = new SyncTransfer(new Host("t"), p);
        final TransferPathFilter filter = t.filter(new NullSession(new Host("t")), TransferAction.upload);
        final Path test = new Path(p, "a", Path.FILE_TYPE);
        test.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
        assertTrue(filter.accept(test, new TransferStatus().exists(true)));
    }

    @Test
    public void testFilterMirror() throws Exception {
        final Path p = new Path("t", Path.DIRECTORY_TYPE);
        p.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
        Transfer t = new SyncTransfer(new Host("t"), p);
        final TransferPathFilter filter = t.filter(new NullSession(new Host("t")), TransferAction.mirror);
        final Path test = new Path(p, "a", Path.FILE_TYPE);
        test.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
        assertTrue(filter.accept(test, new TransferStatus().exists(true)));
    }

    @Test
    public void testChildrenLocalOnly() throws Exception {
        final Path root = new Path("t", Path.DIRECTORY_TYPE);
        root.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                final AttributedList<Local> list = new AttributedList<Local>();
                list.add(new NullLocal(System.getProperty("java.io.tmpdir") + "/t", "a"));
                return list;
            }
        });
        final NullSession session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        };
        Transfer t = new SyncTransfer(new Host("t"), root);
        final AttributedList<Path> list = t.list(session, root, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertFalse(t.filter(session, TransferAction.download).accept(root, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.upload).accept(root, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.mirror).accept(root, new TransferStatus().exists(true)));
    }

    @Test
    public void testChildrenRemoteOnly() throws Exception {
        final Path root = new Path("t", Path.DIRECTORY_TYPE);
        root.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public AttributedList<Local> list() {
                return new AttributedList<Local>();
            }
        });
        final Path a = new Path(root, "a", Path.FILE_TYPE);
        a.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t"));
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
        Transfer t = new SyncTransfer(new Host("t"), root);
        final AttributedList<Path> list = t.list(session, root, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertFalse(t.filter(session, TransferAction.download).accept(root, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.upload).accept(root, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.mirror).accept(root, new TransferStatus().exists(true)));
        assertTrue(t.filter(session, TransferAction.download).accept(a, new TransferStatus().exists(true)));
        // Because root is directory
        assertTrue(t.filter(session, TransferAction.upload).accept(root, new TransferStatus().exists(true)));
        assertFalse(t.filter(session, TransferAction.upload).accept(a, new TransferStatus().exists(true)));
    }
}
