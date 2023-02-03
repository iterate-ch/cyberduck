package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import com.google.api.services.drive.model.File;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultAttributesFinderFeatureTest extends AbstractDriveTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        new DefaultAttributesFinderFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testAttributes() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final AttributesFinder f = new DefaultAttributesFinderFeature(session);
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(
                DriveHomeFinderService.MYDRIVE_FOLDER, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final String initialFileid = file.attributes().getFileId();
        assertNotNull(initialFileid);
        assertNotSame(file.attributes(), f.find(file));
        assertEquals(0L, f.find(file).getSize());
        // Test cache
        assertEquals(0L, f.find(file).getSize());
        // Test wrong type
        try {
            f.find(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, name, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        // Overwrite with new version
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(12);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        status.setLength(content.length);
        final HttpResponseOutputStream<File> out = new DriveWriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
        IOUtils.copy(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(initialFileid, f.find(file.withAttributes(new PathAttributes(file.attributes()).withFileId(initialFileid))).getFileId());
        final String newFileid = out.getStatus().getId();
        assertEquals(newFileid, f.find(file.withAttributes(new PathAttributes(file.attributes()).withFileId(newFileid))).getFileId());
        assertNotEquals(initialFileid, f.find(file.withAttributes(new PathAttributes(file.attributes()).withFileId(newFileid))).getFileId());
        assertEquals(out.getStatus().getId(), f.find(file).getFileId());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
