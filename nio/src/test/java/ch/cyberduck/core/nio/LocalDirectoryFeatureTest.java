package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalDirectoryFeatureTest {

    @Test
    public void testCreateFolder() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path folder = new Path(new LocalHomeFinderFeature().find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(folder, new TransferStatus());
        assertTrue(Files.exists(session.toPath(folder)));
        assertThrows(ConflictException.class, () -> new LocalDirectoryFeature(session).mkdir(folder, new TransferStatus()));
        new LocalDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(Files.exists(session.toPath(folder)));
    }

    @Test(expected = AccessDeniedException.class)
    public void testFolderExists() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path folder = new Path(new LocalHomeFinderFeature().find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(folder, new TransferStatus());
        new LocalDirectoryFeature(session).mkdir(folder, new TransferStatus());
        new LocalDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
