package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSTouchFeatureTest extends AbstractSDSTest {

    @Test
    public void testSupported() {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        assertTrue(new SDSTouchFeature(session, nodeid).isSupported(new Path(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), StringUtils.EMPTY));
        assertTrue(new SDSTouchFeature(session, nodeid).isSupported(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), StringUtils.EMPTY));
        assertFalse(new SDSTouchFeature(session, nodeid).isSupported(new Path("/", EnumSet.of(Path.Type.directory)), StringUtils.EMPTY));
    }

    @Test(expected = BackgroundException.class)
    public void testTouchFileRoot() throws Exception {
        try {
            final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
            new SDSTouchFeature(session, nodeid).touch(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Error -80001. Parent ID must be positive. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testInvalidName() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        try {
            final SDSTouchFeature feature = new SDSTouchFeature(session, nodeid);
            assertFalse(feature.isSupported(room, "?"));
            assertThrows(InvalidFilenameException.class, () -> feature.preflight(room, "?"));
            feature.touch(new Path(room, "CON", EnumSet.of(Path.Type.file)), new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Error -40755. Illegal file name='CON'. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
        finally {
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testInvalidCharacter() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        try {
            final SDSTouchFeature feature = new SDSTouchFeature(session, nodeid);
            assertFalse(feature.isSupported(room, "?"));
            assertThrows(InvalidFilenameException.class, () -> feature.preflight(room, "?"));
            feature.touch(new Path(room, "?", EnumSet.of(Path.Type.file)), new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Error -40755. Illegal file name='?'. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
        finally {
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testTouch() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(
            new Path(room, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("text/plain"));
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        assertEquals(test.attributes().getVersionId(), new SDSAttributesFinderFeature(session, nodeid).find(test).getVersionId());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testQuota() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();
        final long quota = 1L + PreferencesFactory.get().getInteger("sds.upload.multipart.chunksize");
        updateRoomRequest.setQuota(quota);
        assertEquals(quota, new NodesApi(session.getClient()).updateRoom(updateRoomRequest, Long.valueOf(room.attributes().getVersionId()), StringUtils.EMPTY, null).getQuota(), 0L);
        assertTrue(new SDSTouchFeature(session, nodeid).isSupported(room.withAttributes(new SDSAttributesFinderFeature(session, nodeid).find(room)), StringUtils.EMPTY));
        assertEquals(quota, room.attributes().getQuota());
        final byte[] content = RandomUtils.nextBytes(2);
        final TransferStatus status = new TransferStatus();
        status.setLength(2L);
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        PathAttributes attr;
        final long timestamp = System.currentTimeMillis();
        do {
            attr = new SDSAttributesFinderFeature(session, nodeid).find(room);
            if(System.currentTimeMillis() - timestamp > Duration.ofMinutes(1L).toMillis()) {
                fail();
            }
        }
        while(attr.getSize() != 2L);
        assertFalse(new SDSTouchFeature(session, nodeid).isSupported(room.withAttributes(attr), StringUtils.EMPTY));
        assertEquals(quota, attr.getQuota());
        assertEquals(2L, attr.getSize());
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(test, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
