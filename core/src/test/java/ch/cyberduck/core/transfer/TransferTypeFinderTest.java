package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.local.DefaultTemporaryFileService;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class TransferTypeFinderTest {

    @Test
    public void testTypeSingleFile() throws Exception {
        final Host host = new Host(new TestProtocol(), "h") {
            @Override
            public TransferType getTransferType() {
                return Host.TransferType.concurrent;
            }
        };
        final Host.TransferType type = new TransferTypeFinder().type(Host.TransferType.unknown, new DownloadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.file)),
                new NullLocal("/t")));
        assertEquals(Host.TransferType.concurrent, type);
    }

    @Test
    public void testTypeMultipleFilesConcurrent() throws Exception {
        final Host host = new Host(new TestProtocol(), "h") {
            @Override
            public TransferType getTransferType() {
                return Host.TransferType.concurrent;
            }
        };
        Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final Host.TransferType type = new TransferTypeFinder().type(Host.TransferType.unknown, new DownloadTransfer(host,
                Arrays.asList(
                        new TransferItem(file, new DefaultTemporaryFileService().create(file)),
                        new TransferItem(file, new DefaultTemporaryFileService().create(file))
                )
        ));
        assertEquals(Host.TransferType.concurrent, type);
    }

    @Test
    public void testTypeMultipleFilesSingle() throws Exception {
        final Host host = new Host(new TestProtocol(), "h") {
            @Override
            public TransferType getTransferType() {
                return Host.TransferType.newconnection;
            }
        };
        Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final Host.TransferType type = new TransferTypeFinder().type(Host.TransferType.unknown, new DownloadTransfer(host,
                Arrays.asList(
                        new TransferItem(file, new DefaultTemporaryFileService().create(file)),
                        new TransferItem(file, new DefaultTemporaryFileService().create(file))
                )
        ));
        assertEquals(Host.TransferType.newconnection, type);
    }

    @Test
    public void testTypeSingleFolder() throws Exception {
        final Host host = new Host(new TestProtocol(), "h") {
            @Override
            public TransferType getTransferType() {
                return Host.TransferType.concurrent;
            }
        };
        final Host.TransferType type = new TransferTypeFinder().type(Host.TransferType.unknown, new UploadTransfer(host,
                Collections.singletonList(
                        new TransferItem(new Path("/t", EnumSet.of(Path.Type.directory)), new NullLocal("/t"))
                )
        ));
        assertEquals(Host.TransferType.concurrent, type);
    }
}