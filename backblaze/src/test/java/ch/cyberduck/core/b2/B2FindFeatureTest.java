package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.B2StartLargeFileResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class B2FindFeatureTest extends AbstractB2Test {

    @Test
    public void testFind() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2TouchFeature(session, fileid).touch(file, new TransferStatus());
        assertTrue(new B2FindFeature(session, fileid).find(file));
        assertFalse(new B2FindFeature(session, fileid).find(new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindLargeUpload() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.upload));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                new B2VersionIdProvider(session).getVersionId(bucket),
                file.getName(), null, Collections.emptyMap());
        assertTrue(new B2FindFeature(session, new B2VersionIdProvider(session)).find(file));
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
    }
}
