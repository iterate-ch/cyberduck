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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
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
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultAttributesFinderFeatureTest extends AbstractS3Test {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        new DefaultAttributesFinderFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testAttributes() throws Exception {
        final AttributesFinder f = new DefaultAttributesFinderFeature(session);
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path file = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        final String initialVersion = file.attributes().getVersionId();
        assertNotNull(initialVersion);
        assertNotSame(file.attributes(), f.find(file));
        assertEquals(0L, f.find(file).getSize());
        // Test cache
        assertEquals(0L, f.find(file).getSize());
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
        final HttpResponseOutputStream<StorageObject> out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(file, status, new DisabledConnectionCallback());
        IOUtils.copy(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(initialVersion, f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(initialVersion))).getVersionId());
        final String newVersion = ((S3Object) out.getStatus()).getVersionId();
        assertEquals(newVersion, f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(newVersion))).getVersionId());
        assertNotEquals(initialVersion, f.find(file.withAttributes(new PathAttributes(file.attributes()).withVersionId(newVersion))).getVersionId());
        assertEquals(new S3AttributesAdapter().toAttributes(out.getStatus()).getVersionId(), f.find(file).getVersionId());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
