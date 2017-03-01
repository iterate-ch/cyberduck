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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalDirectoryFeatureTest {

    @Test
    public void testCreateFolder() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path folder = new Path(new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(),
                EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        assertTrue(Files.exists(Paths.get(folder.getAbsolute())));
        new LocalDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(Files.exists(Paths.get(folder.getAbsolute())));
    }

    @Test(expected = AccessDeniedException.class)
    public void testFolderExists() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path folder = new Path(new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(),
                EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        new LocalDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        new LocalDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}