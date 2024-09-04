package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DefaultFindFeatureTest extends AbstractS3Test {

    @Test
    public void testFind() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertFalse(new DefaultFindFeature(session).find(file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(file, new TransferStatus());
        // Find without version id set in attributes
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(file));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
