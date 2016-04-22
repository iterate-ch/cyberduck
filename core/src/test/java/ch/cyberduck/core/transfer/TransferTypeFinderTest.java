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
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class TransferTypeFinderTest {

    @Test
    public void testTypeSingleFile() throws Exception {
        final Host host = new Host(new TestProtocol(), "h");
        final Host.TransferType type = new TransferTypeFinder().type(new NullSession(host) {
            @Override
            public Host.TransferType getTransferType() {
                return Host.TransferType.concurrent;
            }
        }, new DownloadTransfer(host,
                new Path("/t", EnumSet.of(Path.Type.file)),
                new NullLocal("/t")));
        assertEquals(Host.TransferType.concurrent, type);
    }

    @Test
    public void testTypeMultipleFilesConcurrent() throws Exception {
        final Host host = new Host(new TestProtocol(), "h");
        final Host.TransferType type = new TransferTypeFinder().type(new NullSession(host) {
            @Override
            public Host.TransferType getTransferType() {
                return Host.TransferType.concurrent;
            }
        }, new DownloadTransfer(host,
                Arrays.asList(
                        new TransferItem(new Path("/t", EnumSet.of(Path.Type.file)), new NullLocal("/t")),
                        new TransferItem(new Path("/t", EnumSet.of(Path.Type.file)), new NullLocal("/t"))
                )
        ));
        assertEquals(Host.TransferType.concurrent, type);
    }

    @Test
    public void testTypeMultipleFilesSingle() throws Exception {
        final Host host = new Host(new TestProtocol(), "h");
        final Host.TransferType type = new TransferTypeFinder().type(new NullSession(host) {
            @Override
            public Host.TransferType getTransferType() {
                return Host.TransferType.newconnection;
            }
        }, new DownloadTransfer(host,
                Arrays.asList(
                        new TransferItem(new Path("/t", EnumSet.of(Path.Type.file)), new NullLocal("/t")),
                        new TransferItem(new Path("/t", EnumSet.of(Path.Type.file)), new NullLocal("/t"))
                )
        ));
        assertEquals(Host.TransferType.newconnection, type);
    }

    @Test
    public void testTypeSingleFolder() throws Exception {
        final Host host = new Host(new TestProtocol(), "h");
        final Host.TransferType type = new TransferTypeFinder().type(new NullSession(host) {
            @Override
            public Host.TransferType getTransferType() {
                return Host.TransferType.concurrent;
            }
        }, new UploadTransfer(host,
                Collections.singletonList(
                        new TransferItem(new Path("/t", EnumSet.of(Path.Type.directory)), new NullLocal("/t"))
                )
        ));
        assertEquals(Host.TransferType.concurrent, type);
    }
}