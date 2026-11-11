package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVLockFeature;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class DAVLockFeatureTest extends AbstractNextcloudTest {

    /**
     * Locking is advertised in the capabilities with the files_lock app installed
     */
    @Test
    public void testLockAdvertised() {
        assertTrue(session.ocs.locking);
        assertNotNull(session.getFeature(Lock.class));
    }

    @Test
    public void testLockUnlock() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new NextcloudWriteFeature(session),
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)),
                new TransferStatus());
        final Lock<String> feature = new DAVLockFeature(session);
        assertNull(new NextcloudAttributesFinderFeature(session).find(test).getLockId());
        final String lock = feature.lock(test);
        assertNotNull(lock);
        assertEquals(lock, new NextcloudAttributesFinderFeature(session).find(test).getLockId());
        // Deleting a locked file without the lock token is rejected
        assertThrows(LockedException.class, () -> new DAVDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
        assertTrue(new DAVFindFeature(session).find(test));
        // Succeeds with the lock token
        new DAVDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus().setLockId(lock)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DAVFindFeature(session).find(test));
    }
}
