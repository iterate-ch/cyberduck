package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import com.google.api.services.storage.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageAttributesFinderFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testFindRoot() throws Exception {
        final GoogleStorageAttributesFinderFeature f = new GoogleStorageAttributesFinderFeature(session);
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testFindBucket() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new GoogleStorageAttributesFinderFeature(session).find(container);
        assertNotSame(PathAttributes.EMPTY, attributes);
        assertEquals(-1L, attributes.getSize());
        assertNotNull(attributes.getRegion());
        assertNotNull(attributes.getETag());
        assertEquals(attributes, new GoogleStorageBucketListService(session).list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener()).find(new SimplePathPredicate(container)).attributes());
    }

    @Test
    public void testPreviousVersionReferences() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String versionId = new GoogleStorageAttributesFinderFeature(session).find(test).getVersionId();
        assertEquals(test.attributes().getVersionId(), versionId);
        final byte[] content = RandomUtils.nextBytes(512);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final Path update = new Path(container, test.getName(), test.getType(),
                new PathAttributes().withVersionId(String.valueOf(out.getStatus().getGeneration())));
        final PathAttributes attributes = new GoogleStorageAttributesFinderFeature(session).find(update);
        assertFalse(attributes.isDuplicate());
        final AttributedList<Path> versions = new GoogleStorageVersioningFeature(session).list(update, new DisabledListProgressListener());
        assertEquals(1, versions.size());
        assertFalse(versions.isEmpty());
        assertEquals(new Path(test).withAttributes(new PathAttributes(test.attributes()).withVersionId(versionId)), versions.get(0));
        for(Path version : versions) {
            assertTrue(version.attributes().isDuplicate());
        }
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleted() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(PathAttributes.EMPTY, new GoogleStorageAttributesFinderFeature(session).find(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        try {
            new GoogleStorageAttributesFinderFeature(session).find(test);
            fail();
        }
        catch(NotfoundException e) {
            throw e;
        }
    }

    @Test(expected = NotfoundException.class)
    public void testDeletedWithMarker() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(PathAttributes.EMPTY, new GoogleStorageAttributesFinderFeature(session).find(test));
        // Add delete marker
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(new Path(test).withAttributes(PathAttributes.EMPTY)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertTrue(new GoogleStorageAttributesFinderFeature(session).find(test).isDuplicate());
        try {
            // Noncurrent versions only appear in requests that explicitly call for object versions to be included
            new GoogleStorageAttributesFinderFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY));
            fail();
        }
        catch(NotfoundException e) {
            throw e;
        }
    }

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new GoogleStorageFindFeature(session).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new GoogleStorageTouchFeature(session).touch(
                new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new GoogleStorageAttributesFinderFeature(session).find(test));
        assertNotNull(new GoogleStorageAttributesFinderFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        try {
            new GoogleStorageAttributesFinderFeature(session).find(test);
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            new GoogleStorageAttributesFinderFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }
}
