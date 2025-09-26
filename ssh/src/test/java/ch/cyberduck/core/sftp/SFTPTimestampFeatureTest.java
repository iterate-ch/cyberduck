package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class SFTPTimestampFeatureTest extends AbstractSFTPTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(new SFTPWriteFeature(session), test, new TransferStatus());
        final long modified = System.currentTimeMillis();
        new SFTPTimestampFeature(session).setTimestamp(test, modified);
        assertEquals(TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(modified)), new SFTPAttributesFinderFeature(session).find(test).getModificationDate());
        assertEquals(TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(modified)), new DefaultAttributesFinderFeature(session).find(test).getModificationDate());
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());

    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new SFTPDirectoryFeature(session).mkdir(new SFTPWriteFeature(session), test, new TransferStatus());
        final long modified = System.currentTimeMillis();
        new SFTPTimestampFeature(session).setTimestamp(test, modified);
        assertEquals(TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(modified)), new SFTPListService(session).list(home, new DisabledListProgressListener()).get(test).attributes().getModificationDate(), 0);
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());

    }
}
