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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.NotfoundException;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSAttributesFinderFeatureTest extends AbstractSDSTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
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
    public void testFindFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getChecksum().algorithm);
        assertTrue(attributes.getPermission().isReadable());
        assertTrue(attributes.getPermission().isWritable());
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
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
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
    public void testBranchVersion() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final PathAttributes previous = f.find(folder);
        assertNotEquals(-1L, previous.getRevision().longValue());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(test.getParent().equals(folder));
        assertTrue(new SDSFindFeature(nodeid).find(test));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final SDSWriteFeature writer = new SDSWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertEquals(previous.getModificationDate(), new SDSAttributesFinderFeature(session, nodeid).find(folder).getModificationDate());
        assertNotEquals(previous.getRevision(), new SDSAttributesFinderFeature(session, nodeid).find(folder).getRevision());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
