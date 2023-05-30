package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class S3TimestampFeatureTest extends AbstractS3Test {

    @Test
    public void testFindTimesteamp() throws Exception {
        final Path bucket = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final TransferStatus status = new TransferStatus();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status.withTimestamp(1530305150672L));
        assertEquals(1530305150000L, status.getResponse().getModificationDate());
        assertEquals(1530305150000L, new S3AttributesFinderFeature(session, acl).find(test).getModificationDate());
        final S3TimestampFeature feature = new S3TimestampFeature(session);
        feature.setTimestamp(test, 1630305150672L);
        assertEquals(1630305150000L, new S3AttributesFinderFeature(session, acl).find(test).getModificationDate());
        test.attributes().setModificationDate(1630305150000L);
        final Path found = new S3ObjectListService(session, acl, true).list(bucket, new DisabledListProgressListener()).find(new DefaultPathPredicate(test));
        assertEquals(1630305150000L, found.attributes().getModificationDate());
        final Path moved = new S3MoveFeature(session, acl).move(test, new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status, new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(1630305150000L, moved.attributes().getModificationDate());
        assertEquals(1630305150000L, new S3AttributesFinderFeature(session, acl).find(moved).getModificationDate());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(moved), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
