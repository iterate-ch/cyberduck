package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesAdapter;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CachingAttributesFinderFeatureTest extends AbstractS3Test {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final PathCache cache = new PathCache(1);
        final CachingAttributesFinderFeature f = new CachingAttributesFinderFeature(session, cache, new DefaultAttributesFinderFeature(session));
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        f.find(test);
        // Test cache
        new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return PathAttributes.EMPTY;
            }
        }).find(test);
    }

    @Test
    public void testDefaultAttributes() throws Exception {
        final PathCache cache = new PathCache(1);
        final AttributesFinder f = new CachingAttributesFinderFeature(session, cache, new DefaultAttributesFinderFeature(session));
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path file = new S3TouchFeature(session, acl).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final String initialVersion = file.attributes().getVersionId();
        assertNotNull(initialVersion);
        final PathAttributes lookup = f.find(file);
        assertNotSame(file.attributes(), lookup);
        assertEquals(0L, lookup.getSize());
        // Test with no specific version id
        assertSame(lookup, f.find(new Path(file).withAttributes(PathAttributes.EMPTY)));
        // Test cache
        assertEquals(0L, new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return PathAttributes.EMPTY;
            }
        }).find(file).getSize());
        // Test wrong type
        try {
            f.find(new Path(bucket, name, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        // Overwrite with new version
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(12);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        status.setLength(content.length);
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session, acl).write(file, status, new DisabledConnectionCallback());
        IOUtils.copy(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(initialVersion, f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(initialVersion))).getVersionId());
        final String newVersion = ((S3Object) out.getStatus()).getVersionId();
        try {
            f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(newVersion)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        cache.clear();
        assertEquals(newVersion, f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(newVersion))).getVersionId());
        assertEquals(newVersion, f.find(file.withAttributes(PathAttributes.EMPTY)).getVersionId());
        assertNotEquals(initialVersion, f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(newVersion))).getVersionId());
        assertEquals(new S3AttributesAdapter(session.getHost()).toAttributes(out.getStatus()).getVersionId(), f.find(file).getVersionId());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
