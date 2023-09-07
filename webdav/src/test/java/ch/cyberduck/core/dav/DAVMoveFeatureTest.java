package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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
public class DAVMoveFeatureTest extends AbstractDAVTest {

    @Test
    public void testMove() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0L, test.attributes().getSize());
        final TransferStatus status = new TransferStatus();
        new DAVTimestampFeature(session).setTimestamp(test, status.withModified(5000L));
        final PathAttributes attr = new DAVAttributesFinderFeature(session).find(test);
        final Path target = new DAVMoveFeature(session).move(test.withAttributes(status.getResponse()),
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        assertEquals(status.getResponse(), target.attributes());
        assertEquals(attr, new DAVAttributesFinderFeature(session).find(target));
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveWithLock() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        String lock = null;
        try {
            lock = new DAVLockFeature(session).lock(test);
        }
        catch(InteroperabilityException e) {
            // Not supported
        }
        assertEquals(0L, test.attributes().getSize());
        final Path target = new DAVMoveFeature(session).move(test,
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLockId(lock), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        assertEquals(test.attributes(), target.attributes());
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final Path folder = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DAVDirectoryFeature(session).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        {
            final byte[] content = RandomUtils.nextBytes(3547);
            final TransferStatus status = new TransferStatus();
            status.setOffset(0L);
            status.setLength(1024L);
            final HttpResponseOutputStream<Void> out = new DAVWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            // Write first 1024
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final PathAttributes attr = new DAVAttributesFinderFeature(session).find(test);
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DAVMoveFeature(session).move(folder, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(folder));
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        assertTrue(new DAVFindFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))));
        assertEquals(attr, new DAVAttributesFinderFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))));
        assertEquals(attr.getModificationDate(), new DAVAttributesFinderFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))).getModificationDate());
        assertNotEquals(attr.getETag(), new DAVAttributesFinderFeature(session).find(new Path(target, test.getName(), EnumSet.of(Path.Type.file))).getETag());
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DAVTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DAVTouchFeature(session).touch(target, new TransferStatus());
        assertThrows(ConflictException.class, () -> new DAVMoveFeature(session).move(test, target, new TransferStatus().exists(false), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        new DAVMoveFeature(session).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(target));
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DAVMoveFeature(session).move(test, new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}
