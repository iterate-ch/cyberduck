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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class LocalSymlinkFeatureTest {

    @Test
    public void testSymlink() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assumeTrue(session.isPosixFilesystem());
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(target, new TransferStatus());
        final Path link = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        new LocalSymlinkFeature(session).symlink(link, target.getName());
        assertTrue(new LocalFindFeature(session).find(link));
        assertEquals(EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink),
                new LocalListService(session).list(workdir, new DisabledListProgressListener()).get(link).getType());
        new LocalDeleteFeature(session).delete(Collections.singletonList(link), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new LocalFindFeature(session).find(link));
        assertTrue(new LocalFindFeature(session).find(target));
        new LocalDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
