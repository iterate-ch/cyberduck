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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSAttributesFinderFeatureTest extends AbstractSDSTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        try {
            f.find(test);
        }
        finally {
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testFindRoot() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final PathAttributes attributes = f.find(new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory)));
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertNotEquals(Acl.EMPTY, attributes.getAcl());
    }

    @Test
    public void testFindFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final PathAttributes attributes = f.find(test);
        assertNotNull(attributes.getRevision());
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertEquals(Checksum.NONE, attributes.getChecksum());
        assertTrue(attributes.getPermission().isReadable());
        assertTrue(attributes.getPermission().isWritable());
        assertNotNull(attributes.getCustom().get(SDSAttributesFinderFeature.KEY_CLASSIFICATION));
        assertNotNull(attributes.getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED));
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNull(attributes.getChecksum().algorithm);
        assertTrue(attributes.getPermission().isReadable());
        assertTrue(attributes.getPermission().isWritable());
        assertTrue(attributes.getPermission().isExecutable());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final PathAttributes attributes = new SDSAttributesFinderFeature(session, nodeid).find(room);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNull(attributes.getChecksum().algorithm);
        assertTrue(attributes.getPermission().isReadable());
        assertTrue(attributes.getPermission().isWritable());
        assertTrue(attributes.getPermission().isExecutable());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testVersioning() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final PathAttributes previous = f.find(folder, new DisabledListProgressListener());
        assertNotEquals(-1L, previous.getRevision().longValue());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String initialVersion = test.attributes().getVersionId();
        assertEquals(test.getParent(), folder);
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setExists(true);
        status.setLength(content.length);
        final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(initialVersion, test.attributes().getVersionId());
        final PathAttributes updated = new SDSAttributesFinderFeature(session, nodeid).find(test, new DisabledListProgressListener());
        assertEquals(status.getResponse().getVersionId(), updated.getVersionId());
        assertEquals(previous.getModificationDate(), new SDSAttributesFinderFeature(session, nodeid).find(folder, new DisabledListProgressListener()).getModificationDate());
        assertEquals(previous.getChecksum(), new SDSAttributesFinderFeature(session, nodeid).find(folder, new DisabledListProgressListener()).getChecksum());
        assertEquals(previous.getModificationDate(), new SDSAttributesFinderFeature(session, nodeid).find(folder, new DisabledListProgressListener()).getModificationDate());
        // Branch version is changing with background task only
        // assertNotEquals(previous.getRevision(), new SDSAttributesFinderFeature(session, nodeid).find(folder, new DisabledListProgressListener()).getRevision());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testChangedNodeId() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestnodeid = test.attributes().getVersionId();
        assertNotNull(latestnodeid);
        // Assume previously seen but changed on server
        final String invalidId = String.valueOf(RandomUtils.nextLong());
        test.attributes().setVersionId(invalidId);
        nodeid.cache(test, invalidId, null);
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        assertEquals(latestnodeid, f.find(test).getVersionId());
        assertEquals(latestnodeid, test.attributes().getVersionId());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
