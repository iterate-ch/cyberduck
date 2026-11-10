package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVLockFeature;
import ch.cyberduck.core.dav.DAVMoveFeature;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class OwncloudLockFeatureTest extends AbstractOwncloudTest {

    /**
     * Locking is not advertised in the capabilities of OpenCloud but implemented
     */
    @Test
    public void testLockAvailable() {
        assertTrue(session.ocs.locking);
        assertNotNull(session.getFeature(Lock.class));
    }

    @Test
    public void testMoveWriteLocked() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new DAVTouchFeature(session).touch(new NextcloudWriteFeature(session),
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)),
                new TransferStatus());
        final String lock = new DAVLockFeature(session).lock(test);
        assertNotNull(lock);
        // Overwrite with lock token
        final byte[] content = RandomUtils.nextBytes(32);
        final TransferStatus status = new TransferStatus().setExists(true).setLength(content.length).setLockId(lock);
        final StatusOutputStream<?> out = new NextcloudWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        IOUtils.write(content, out);
        out.close();
        // Move with lock token
        final Path renamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertThrows(LockedException.class, () -> new DAVMoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        new DAVMoveFeature(session).move(test, renamed, new TransferStatus().setLockId(lock), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(renamed));
        new OwncloudDeleteFeature(session).delete(Collections.singletonMap(renamed, new TransferStatus().setLockId(lock)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testLockUnlock()throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new NextcloudWriteFeature(session),
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)),
                new TransferStatus());
        final DAVLockFeature feature = new DAVLockFeature(session);
        assertNull(new OwncloudAttributesFinderFeature(session).find(test).getLockId());
        final String lock = feature.lock(test);
        assertNotNull(lock);
        assertEquals(lock, new OwncloudAttributesFinderFeature(session).find(test).getLockId());
        // Deleting a locked file without the lock token is rejected
        assertThrows(LockedException.class, () -> new OwncloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
        assertTrue(new DAVFindFeature(session).find(test));
        // Succeeds with the lock token
        new OwncloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus().setLockId(lock)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DAVFindFeature(session).find(test));
    }
}
