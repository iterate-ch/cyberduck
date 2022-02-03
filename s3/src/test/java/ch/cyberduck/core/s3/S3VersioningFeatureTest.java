package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.MultipartUpload;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3VersioningFeatureTest extends AbstractS3Test {

    @Test
    public void testGetConfigurationDisabled() throws Exception {
        final VersioningConfiguration configuration
            = new S3VersioningFeature(session, new S3AccessControlListFeature(session)).getConfiguration(new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertNotNull(configuration);
        assertFalse(configuration.isEnabled());
        assertFalse(configuration.isMultifactor());
    }

    @Test
    public void testGetConfigurationEnabled() throws Exception {
        final VersioningConfiguration configuration
            = new S3VersioningFeature(session, new S3AccessControlListFeature(session)).getConfiguration(new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertNotNull(configuration);
        assertTrue(configuration.isEnabled());
    }

    @Test
    public void testSetConfiguration() throws Exception {
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(container, new TransferStatus());
        final Versioning feature = new S3VersioningFeature(session, new S3AccessControlListFeature(session));
        feature.setConfiguration(container, new DisabledLoginCallback(), new VersioningConfiguration(true, false));
        assertTrue(feature.getConfiguration(container).isEnabled());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testForbidden() throws Exception {
        session.getHost().getCredentials().setPassword(StringUtils.EMPTY);
        assertEquals(VersioningConfiguration.empty(),
            new S3VersioningFeature(session, new S3AccessControlListFeature(session)).getConfiguration(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testRevert() throws Exception {
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(
                bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final S3AttributesFinderFeature f = new S3AttributesFinderFeature(session);
        final Path test = new S3TouchFeature(session).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final String initialVersion = test.attributes().getVersionId();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final S3MultipartWriteFeature writer = new S3MultipartWriteFeature(session);
        final StatusOutputStream<MultipartUpload> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final PathAttributes updated = new S3AttributesFinderFeature(session, true).find(new Path(test).withAttributes(PathAttributes.EMPTY));
        assertNotEquals(initialVersion, updated.getVersionId());
        assertFalse(updated.getVersions().isEmpty());
        assertEquals(1, updated.getVersions().size());
        assertEquals(new Path(test).withAttributes(initialAttributes), updated.getVersions().get(0));
        assertTrue(new S3FindFeature(session).find(updated.getVersions().get(0)));
        assertEquals(initialVersion, new S3AttributesFinderFeature(session).find(updated.getVersions().get(0)).getVersionId());
        new S3VersioningFeature(session, new S3AccessControlListFeature(session)).revert(new Path(test).withAttributes(initialAttributes));
        final PathAttributes reverted = new S3AttributesFinderFeature(session, true).find(new Path(test).withAttributes(PathAttributes.EMPTY));
        assertNotEquals(initialVersion, reverted.getVersionId());
        assertEquals(2, reverted.getVersions().size());
        assertEquals(test.attributes().getSize(), reverted.getSize());
        assertEquals(content.length, reverted.getVersions().get(0).attributes().getSize());
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(directory, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
