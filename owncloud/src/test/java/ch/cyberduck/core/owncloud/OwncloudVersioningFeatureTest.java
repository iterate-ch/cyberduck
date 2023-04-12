package ch.cyberduck.core.owncloud;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OwncloudVersioningFeatureTest extends AbstractOwncloudTest {

    @Test
    public void testRevert() throws Exception {
        final Path directory = new DAVDirectoryFeature(session).mkdir(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final NextcloudWriteFeature writer = new NextcloudWriteFeature(session);
        final byte[] initialContent = RandomUtils.nextBytes(32769);
        {
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(initialContent), writer.write(test, status.withLength(initialContent.length), new DisabledConnectionCallback()));
        }
        final OwncloudVersioningFeature feature = new OwncloudVersioningFeature(session);
        assertEquals(0, feature.list(test.withAttributes(new OwncloudAttributesFinderFeature(session).find(test)), new DisabledListProgressListener()).size());
        final PathAttributes initialAttributes = new OwncloudAttributesFinderFeature(session).find(test);
        final byte[] contentUpdate = RandomUtils.nextBytes(16258);
        {
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(contentUpdate), writer.write(test, status.withLength(contentUpdate.length).exists(true), new DisabledConnectionCallback()));
            final AttributedList<Path> versions = feature.list(test.withAttributes(new OwncloudAttributesFinderFeature(session).find(test)), new DisabledListProgressListener());
            assertEquals(1, versions.size());
        }
        {
            final byte[] contentLatest = RandomUtils.nextBytes(13247);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(contentLatest), writer.write(test, status.withLength(contentLatest.length).exists(true), new DisabledConnectionCallback()));
        }
        final AttributedList<Path> versions = feature.list(test.withAttributes(new OwncloudAttributesFinderFeature(session).find(test)), new DisabledListProgressListener());
        assertEquals(2, versions.size());
        final Path initialVersion = versions.get(1);
        {
            assertEquals(initialAttributes.getSize(), initialVersion.attributes().getSize());
            assertEquals(initialAttributes.getModificationDate(), initialVersion.attributes().getModificationDate());
            assertNotNull(initialVersion.attributes().getVersionId());
            assertNotEquals(initialAttributes, new OwncloudAttributesFinderFeature(session).find(test));
            assertEquals(initialVersion.attributes(), new OwncloudAttributesFinderFeature(session).find(initialVersion));
            assertArrayEquals(initialContent, IOUtils.readFully(new OwncloudReadFeature(session).read(initialVersion, new TransferStatus(), new DisabledLoginCallback()),
                    initialContent.length));

            final Path updatedVersion = versions.get(0);
            assertEquals(contentUpdate.length, new OwncloudAttributesFinderFeature(session).find(updatedVersion).getSize());
            assertArrayEquals(contentUpdate, IOUtils.readFully(new OwncloudReadFeature(session).read(updatedVersion, new TransferStatus(), new DisabledLoginCallback()),
                    contentUpdate.length));
        }
        feature.revert(initialVersion);
        assertEquals(initialVersion.attributes().getSize(), new OwncloudAttributesFinderFeature(session).find(test).getSize());
        new DAVDeleteFeature(session).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}