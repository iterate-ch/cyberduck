/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
 */

package ch.cyberduck.core.serializer;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferItemCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class TransferDictionaryTest {

    @BeforeClass
    public static void register() {
        ProtocolFactory.register(new TestProtocol());
    }

    @Test
    public void testSerializeDownloadTransfer() throws Exception {
        final Path test = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host(new TestProtocol(), "t"), test, new NullLocal(UUID.randomUUID().toString(), "transfer"));
        t.addSize(4L);
        t.addTransferred(3L);
        final Transfer serialized = new TransferDictionary().deserialize(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertEquals(t.getRoots(), serialized.getRoots());
        assertEquals(t.getBandwidth(), serialized.getBandwidth());
        assertEquals(4L, serialized.getSize());
        assertEquals(3L, serialized.getTransferred());
        assertFalse(serialized.isComplete());
    }

    @Test
    public void testSerializeUploadTransfer() throws Exception {
        final Path test = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new UploadTransfer(new Host(new TestProtocol(), "t"), test,
                new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
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
    public void testSyncTransfer() throws Exception {
        Transfer t = new SyncTransfer(new Host(new TestProtocol(), "t"),
                new TransferItem(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal(System.getProperty("java.io.tmpdir"), "t")));
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
    public void testSerializeLastSelectedAction() throws Exception {
        final Path p = new Path("t", EnumSet.of(Path.Type.directory));
        final SyncTransfer transfer = new SyncTransfer(new Host(new TestProtocol()), new TransferItem(p, new NullLocal("p", "t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list() throws LocalAccessDeniedException {
                return new AttributedList<Local>(Arrays.<Local>asList(new NullLocal("p", "a")));
            }
        }));
        transfer.action(null, true, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.upload;
            }
        }, new DisabledListProgressListener());
        final Transfer serialized = new TransferDictionary().deserialize(transfer.serialize(SerializerFactory.get()));
        assertNotSame(transfer, serialized);
        assertEquals(TransferAction.upload, serialized.action(null, true, false, new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                fail();
                return null;
            }
        }, new DisabledListProgressListener()));
    }

    @Test
    public void testSerializeComplete() throws Exception {
        // Test transfer to complete with existing directory
        final Host host = new Host(new TestProtocol());
        final Transfer t = new DownloadTransfer(host, new Path("/t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public AttributedList<Local> list(final Filter<String> filter) throws AccessDeniedException {
                return AttributedList.emptyList();
            }

            @Override
            public boolean isFile() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return true;
            }
        });
        final NullSession session = new NullSession(host);
        new SingleTransferWorker(session, t, new TransferOptions(),
                new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(), new DisabledTransferItemCallback(),
                new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback()).run(session);
        assertTrue(t.isComplete());
        final Transfer serialized = new TransferDictionary().deserialize(t.serialize(SerializerFactory.get()));
        assertNotSame(t, serialized);
        assertTrue(serialized.isComplete());
    }
}