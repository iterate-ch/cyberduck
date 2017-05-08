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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class LocalSymlinkFeatureTest {

    @Test
    public void testSymlink() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path(new TemporarySupportDirectoryFinder().find().getAbsolute(), EnumSet.of(Path.Type.directory));
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(target, new TransferStatus());
        final Path link = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        new LocalSymlinkFeature(session).symlink(link, target.getName());
        assertTrue(new LocalFindFeature(session).find(link));
        assertEquals(EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink),
                session.list(workdir, new DisabledListProgressListener()).get(link).getType());
        new LocalDeleteFeature(session).delete(Collections.singletonList(link), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new LocalFindFeature(session).find(link));
        assertTrue(new LocalFindFeature(session).find(target));
        new LocalDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}