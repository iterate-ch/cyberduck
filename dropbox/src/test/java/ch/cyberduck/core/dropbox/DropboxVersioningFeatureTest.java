package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.dropbox.core.v2.files.Metadata;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxVersioningFeatureTest extends AbstractDropboxTest {

    @Test
    public void testRevert() throws Exception {
        final Path directory = new DropboxDirectoryFeature(session).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final DropboxAttributesFinderFeature f = new DropboxAttributesFinderFeature(session);
        final Path test = new DropboxTouchFeature(session).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(test.attributes().getVersionId(), new DropboxAttributesFinderFeature(session).find(test).getVersionId());
        final DropboxVersioningFeature feature = new DropboxVersioningFeature(session);
        assertEquals(0, feature.list(test, new DisabledListProgressListener()).size());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final String initialVersion = test.attributes().getVersionId();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final DropboxWriteFeature writer = new DropboxWriteFeature(session);
        final StatusOutputStream<Metadata> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotEquals(initialVersion, new DropboxAttributesFinderFeature(session).toAttributes(out.getStatus()).getVersionId());
        {
            final AttributedList<Path> versions = feature.list(test.withAttributes(new DropboxAttributesFinderFeature(session).toAttributes(out.getStatus())), new DisabledListProgressListener());
            assertEquals(1, versions.size());
            assertEquals(new Path(test).withAttributes(initialAttributes), versions.get(0));
            assertEquals(initialVersion, versions.get(0).attributes().getVersionId());
        }
        final PathAttributes updated = new DropboxAttributesFinderFeature(session).find(test.withAttributes(new DropboxAttributesFinderFeature(session).toAttributes(out.getStatus())));
        assertNotEquals(initialVersion, updated.getVersionId());
        feature.revert(new Path(test).withAttributes(initialAttributes));
        // Delete versions permanently
        try {
            final List<Path> versions = feature.list(new Path(test).withAttributes(new DropboxAttributesFinderFeature(session).find(test)), new DisabledListProgressListener()).toList();
            assertEquals(2, versions.size());
            assertEquals(status.getResponse().getVersionId(), versions.get(0).attributes().getVersionId());
            assertEquals(initialVersion, versions.get(1).attributes().getVersionId());
            for(Path d : versions) {
                assertFalse(new DropboxThresholdDeleteFeature(session).isSupported(d));
                assertFalse(new DropboxBatchDeleteFeature(session).isSupported(d));
                assertFalse(new DropboxDeleteFeature(session).isSupported(d));
            }
            new DropboxDeleteFeature(session).delete(versions, new DisabledPasswordCallback(), new Delete.DisabledCallback());
            fail();
        }
        catch(InteroperabilityException e) {
            // Expected
        }
        for(Path version : new DropboxListService(session).list(directory, new DisabledListProgressListener())) {
            new DropboxDeleteFeature(session).delete(Collections.singletonList(version), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}