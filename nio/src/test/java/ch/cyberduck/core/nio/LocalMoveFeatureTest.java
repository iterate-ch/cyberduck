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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalMoveFeatureTest {

    @Test
    public void testMove() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalMoveFeature(session).move(test, target, false, new Delete.DisabledCallback());
        assertFalse(new LocalFindFeature(session).find(test));
        assertTrue(new LocalFindFeature(session).find(target));
        new LocalDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(target, new TransferStatus());
        new LocalMoveFeature(session).move(test, target, true, new Delete.DisabledCallback());
        assertFalse(new LocalFindFeature(session).find(test));
        assertTrue(new LocalFindFeature(session).find(target));
        new LocalDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalMoveFeature(session).move(test, new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), false, new Delete.DisabledCallback());
    }
}