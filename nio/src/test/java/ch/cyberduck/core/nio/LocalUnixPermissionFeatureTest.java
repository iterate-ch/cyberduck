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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalUnixPermissionFeatureTest {

    @Test
    public void testSetUnixPermission() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        {
            final Path file = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
            new LocalTouchFeature(session).touch(file, new TransferStatus());
            new LocalUnixPermissionFeature(session).setUnixPermission(file, new Permission(666));
            assertEquals("666", session.list(workdir, new DisabledListProgressListener()).get(file).attributes().getPermission().getMode());
            new LocalDeleteFeature(session).delete(Collections.<Path>singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        {
            final Path directory = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
            new LocalDirectoryFeature(session).mkdir(directory, null, new TransferStatus());
            new LocalUnixPermissionFeature(session).setUnixPermission(directory, new Permission(666));
            assertEquals("666", session.list(workdir, new DisabledListProgressListener()).get(directory).attributes().getPermission().getMode());
            new LocalDeleteFeature(session).delete(Collections.<Path>singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        session.close();
    }
}