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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSNodeIdProviderTest extends AbstractSDSTest {

    @Test
    public void getFileIdFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final String name = String.format("%s%s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random());
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(room, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(nodeid.getFileid(new Path(room, name, EnumSet.of(Path.Type.file)), new DisabledListProgressListener(), 1));
        try {
            assertNull(nodeid.getFileid(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new DisabledListProgressListener(), 1));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            assertNull(nodeid.getFileid(new Path(room, name, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener(), 1));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(file, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdFileVersions() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(room, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final String versionIdTouch = file.attributes().getVersionId();
        assertEquals(versionIdTouch, nodeid.getFileid(new Path(room, name, EnumSet.of(Path.Type.file)), new DisabledListProgressListener(), 1));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(file, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId versionIdWrite = out.getStatus();
        assertNotNull(versionIdWrite);
        assertNotEquals(versionIdTouch, versionIdWrite);
        assertEquals(versionIdWrite.toString(), nodeid.getFileid(new Path(room, name, EnumSet.of(Path.Type.file)), new DisabledListProgressListener(), 1));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertNotNull(nodeid.getFileid(new Path(room, name, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener(), 1));
        try {
            assertNull(nodeid.getFileid(new Path(room, name, EnumSet.of(Path.Type.file)), new DisabledListProgressListener(), 1));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(folder, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final String roomname = new AlphanumericRandomStringService().random();
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(roomname, EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final String subroomname = new AlphanumericRandomStringService().random();
        final Path subroom = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, subroomname, EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        assertNotNull(nodeid.getFileid(new Path(roomname, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener(), 1));
        assertNotNull(nodeid.getFileid(new Path(room, subroomname, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener(), 1));
        try {
            assertNull(nodeid.getFileid(new Path(room, subroomname, EnumSet.of(Path.Type.file)), new DisabledListProgressListener(), 1));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(subroom, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
