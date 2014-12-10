package ch.cyberduck.core.serializer;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.test.NullLocal;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.junit.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class TransferDictionaryTest {

    @Test
    public void testSerializeDownloadTransfer() throws Exception {
        final Path test = new Path("t", EnumSet.of(Path.Type.file));
        Transfer t = new DownloadTransfer(new Host("t"), test, new NullLocal(UUID.randomUUID().toString(), "transfer"));
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
        Transfer t = new UploadTransfer(new Host("t"), test,
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
}