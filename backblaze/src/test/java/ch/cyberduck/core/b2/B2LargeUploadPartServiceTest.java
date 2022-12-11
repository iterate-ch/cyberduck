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

import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2StartLargeFileResponse;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadPartServiceTest extends AbstractB2Test {

    @Test
    public void testFind() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
            new B2VersionIdProvider(session).getVersionId(bucket),
            file.getName(), null, Collections.emptyMap());
        assertEquals(1, new B2LargeUploadPartService(session, new B2VersionIdProvider(session)).find(file).size());
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
    }

    @Test
    public void testFindAllPendingInBucket() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2StartLargeFileResponse start1Response = session.getClient().startLargeFileUpload(
            fileid.getVersionId(bucket),
            file.getName(), null, Collections.emptyMap());
        final B2StartLargeFileResponse start2Response = session.getClient().startLargeFileUpload(
            fileid.getVersionId(bucket),
                file.getName(), null, Collections.emptyMap());
        final List<B2FileInfoResponse> list = new B2LargeUploadPartService(session, fileid).find(file);
        assertFalse(list.isEmpty());
        assertEquals(start2Response.getFileId(), list.get(0).getFileId());
        assertEquals(start1Response.getFileId(), list.get(1).getFileId());
        session.getClient().cancelLargeFileUpload(start1Response.getFileId());
        session.getClient().cancelLargeFileUpload(start2Response.getFileId());
    }

    @Test
    public void testList() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
            new B2VersionIdProvider(session).getVersionId(bucket),
            file.getName(), null, Collections.emptyMap());
        assertTrue(new B2LargeUploadPartService(session, new B2VersionIdProvider(session)).list(startResponse.getFileId()).isEmpty());
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
    }

    @Test
    public void testDelete() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
            new B2VersionIdProvider(session).getVersionId(bucket),
            file.getName(), null, Collections.emptyMap());
        final String fileid = startResponse.getFileId();
        new B2LargeUploadPartService(session, new B2VersionIdProvider(session)).delete(startResponse.getFileId());
    }
}
