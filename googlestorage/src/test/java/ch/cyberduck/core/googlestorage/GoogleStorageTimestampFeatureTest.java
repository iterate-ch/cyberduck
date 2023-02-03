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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
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
public class GoogleStorageTimestampFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testFindTimesteamp() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withTimestamp(1530305150672L));
        assertEquals(1530305150672L, new GoogleStorageAttributesFinderFeature(session).find(test).getModificationDate());
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(1033);
        status.setLength(content.length);
        status.setTimestamp(1530305150673L);
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final PathAttributes response = status.getResponse();
        assertNotNull(response.getETag());
        assertNotNull(response.getVersionId());
        test.withAttributes(response);
        assertEquals(1530305150673L, response.getModificationDate());
        final GoogleStorageTimestampFeature feature = new GoogleStorageTimestampFeature(session);
        // Rewrite object with timestamp earlier than already set
        final TransferStatus rewriteStatus = new TransferStatus().withTimestamp(1530305150672L);
        feature.setTimestamp(test, rewriteStatus);
        final PathAttributes attrAfterRewrite = new GoogleStorageAttributesFinderFeature(session).find(test);
        assertEquals(rewriteStatus.getResponse(), attrAfterRewrite);
        assertEquals(1530305150672L, attrAfterRewrite.getModificationDate());
        assertNotEquals(response.getETag(), attrAfterRewrite.getETag());
        assertNotEquals(response.getVersionId(), attrAfterRewrite.getVersionId());
        final TransferStatus patchStatus = new TransferStatus().withTimestamp(1630305150672L);
        feature.setTimestamp(test, patchStatus);
        assertEquals(1630305150672L, new GoogleStorageAttributesFinderFeature(session).find(test).getModificationDate());
        final PathAttributes attrAfterPatch = new GoogleStorageAttributesFinderFeature(session).find(test);
        assertEquals(patchStatus.getResponse(), attrAfterPatch);
        final String eTagAfterPatch = attrAfterPatch.getETag();
        assertNotEquals(attrAfterRewrite.getETag(), eTagAfterPatch);
        final Path moved = new GoogleStorageMoveFeature(session).move(test, new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(1630305150672L, moved.attributes().getModificationDate());
        assertEquals(1630305150672L, new GoogleStorageAttributesFinderFeature(session).find(moved).getModificationDate());
        assertNotEquals(eTagAfterPatch, new GoogleStorageAttributesFinderFeature(session).find(moved).getETag());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(moved), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
