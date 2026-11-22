package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class S3ListServiceFeaturesTest {

    private final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));

    @Test
    public void testObjectListServiceMetadata() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        assertTrue(new S3ObjectListService(session, acl, true).features(bucket).contains(ListService.Flags.metadata));
        assertFalse(new S3ObjectListService(session, acl, false).features(bucket).contains(ListService.Flags.metadata));
        assertTrue(new S3VersionedObjectListService(session, acl, 1, true).features(bucket).contains(ListService.Flags.metadata));
        assertFalse(new S3VersionedObjectListService(session, acl, 1, false).features(bucket).contains(ListService.Flags.metadata));
    }

    @Test
    public void testListServiceMetadataEnabled() {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setProperty("s3.listing.metadata.enable", String.valueOf(true));
        final S3Session session = new S3Session(host);
        final S3ListService list = new S3ListService(session, new S3AccessControlListFeature(session));
        assertTrue(list.features(bucket).contains(ListService.Flags.metadata));
        // Listing of buckets
        assertFalse(list.features(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))).contains(ListService.Flags.metadata));
    }

    @Test
    public void testListServiceMetadataDisabled() {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        host.setProperty("s3.listing.metadata.enable", String.valueOf(false));
        final S3Session session = new S3Session(host);
        assertFalse(new S3ListService(session, new S3AccessControlListFeature(session)).features(bucket).contains(ListService.Flags.metadata));
    }
}
