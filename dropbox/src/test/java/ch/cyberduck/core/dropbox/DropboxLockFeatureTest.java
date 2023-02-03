package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxLockFeatureTest extends AbstractDropboxTest {

    @Test
    public void testLockNotShared() throws Exception {
        final DropboxTouchFeature touch = new DropboxTouchFeature(session);
        final Path file = touch.touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxLockFeature f = new DropboxLockFeature(session);
        try {
            final String lock = f.lock(file);
            fail();
        }
        catch(UnsupportedException e) {
            //
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testLock() throws Exception {
        final DropboxTouchFeature touch = new DropboxTouchFeature(session);
        final Path file = touch.touch(new Path(new Path(new DefaultHomeFinderService(session).find(), "Projects", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.shared)).withAttributes(new PathAttributes().withFileId("7581509952")),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxLockFeature f = new DropboxLockFeature(session);
        final String lock = f.lock(file);
        assertNotNull(lock);
        assertEquals(lock, new DropboxAttributesFinderFeature(session).find(file).getLockId());
        f.unlock(file, lock);
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Ignore
    @Test(expected = NotfoundException.class)
    public void testLockNoSuchFile() throws Exception {
        final Path file = new Path(new Path(new DefaultHomeFinderService(session).find(), "Projects", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.shared)).withAttributes(new PathAttributes().withFileId("7581509952")),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DropboxLockFeature f = new DropboxLockFeature(session);
        f.lock(file);
    }

    @Test
    public void testLockNotfound() throws Exception {
        final DropboxTouchFeature touch = new DropboxTouchFeature(session);
        final Path file = touch.touch(new Path(new Path(new DefaultHomeFinderService(session).find(), "Projects", EnumSet.of(Path.Type.directory, Path.Type.shared)).withAttributes(new PathAttributes().withFileId("7581509952")),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxLockFeature f = new DropboxLockFeature(session);
        f.unlock(file, "l");
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
