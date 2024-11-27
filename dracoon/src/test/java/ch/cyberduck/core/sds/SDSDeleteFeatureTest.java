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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AntiVirusAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomPoliciesRequest;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSDeleteFeatureTest extends AbstractSDSTest {

    @Test
    public void testDeleteNotFound() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String versionId = file.attributes().getVersionId();
        assertNotNull(versionId);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        try {
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(file.withAttributes(new PathAttributes().withVersionId(versionId))), new DisabledLoginCallback(), new Delete.DisabledCallback());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path fileInRoom = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(fileInRoom, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(fileInRoom));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(fileInRoom), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(fileInRoom));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteRecursively() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path file = new SDSTouchFeature(session, nodeid).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new SDSFindFeature(session, nodeid).find(file));
        assertNotNull(nodeid.getVersionId(file));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        file.attributes().setVersionId(null);
        folder.attributes().setVersionId(null);
        try {
            nodeid.getVersionId(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        try {
            nodeid.getVersionId(folder);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
    }

    @Test
    public void testDeleteFolderRoomWithContent() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room,
                new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(file, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(folder));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(room));
    }

    @Test
    public void testIsRecursive() {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        assertTrue(new SDSDeleteFeature(session, nodeid).isRecursive());
    }

    @Test
    public void testDeleteEicar() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        new NodesApi(session.getClient()).setRoomPolicies(new RoomPoliciesRequest().virusProtectionEnabled(true),
                Long.valueOf(nodeid.getVersionId(room)), StringUtils.EMPTY);
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] eicar = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(eicar, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(eicar.length);
        final SDSDirectS3UploadFeature feature = new SDSDirectS3UploadFeature(session, nodeid, new SDSDelegatingWriteFeature(session, nodeid, new SDSDirectS3WriteFeature(session, nodeid)));
        final Node node = feature.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new SDSFindFeature(session, nodeid).find(test));
        final PathAttributes attributes = new SDSAttributesFinderFeature(session, nodeid).find(test);
        assertEquals(eicar.length, attributes.getSize());
        while(!PathAttributes.Verdict.malicious.equals(new SDSAttributesFinderFeature(session, nodeid).find(test).getVerdict())) {
            // Wait for verdict
        }
        try {
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
            fail();
        }
        catch(AntiVirusAccessDeniedException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(test.withAttributes(new SDSAttributesFinderFeature(session, nodeid).find(test))),
                new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(test));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
