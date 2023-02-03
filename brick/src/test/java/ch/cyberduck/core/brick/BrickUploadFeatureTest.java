package ch.cyberduck.core.brick;

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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BrickUploadFeatureTest extends AbstractBrickTest {

    @Test
    public void testUploadSmallPart() throws Exception {
        final BrickUploadFeature feature = new BrickUploadFeature(session, new BrickWriteFeature(session), 5 * 1024L * 1024L, 2);
        final Path root = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new Path(root, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 56;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setMime("text/plain");
        final BytecountStreamListener count = new BytecountStreamListener();
        feature.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
            count, status, new DisabledLoginCallback());
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertTrue(new BrickFindFeature(session).find(test));
        final PathAttributes attributes = new BrickAttributesFinderFeature(session).find(test);
        assertEquals(content.length, attributes.getSize());
        final byte[] compare = new byte[length];
        IOUtils.readFully(new BrickReadFeature(session).read(test, new TransferStatus().withLength(length), new DisabledConnectionCallback()), compare);
        assertArrayEquals(content, compare);
        new BrickDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadSinglePart() throws Exception {
        final BrickUploadFeature feature = new BrickUploadFeature(session, new BrickWriteFeature(session), 5 * 1024L * 1024L, 2);
        final Path root = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new Path(root, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 2 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setMime("text/plain");
        final BytecountStreamListener count = new BytecountStreamListener();
        feature.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
            count, status, new DisabledLoginCallback());
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertTrue(new BrickFindFeature(session).find(test));
        final PathAttributes attributes = new BrickAttributesFinderFeature(session).find(test);
        assertEquals(content.length, attributes.getSize());
        final byte[] compare = new byte[length];
        IOUtils.readFully(new BrickReadFeature(session).read(test, new TransferStatus().withLength(length), new DisabledConnectionCallback()), compare);
        assertArrayEquals(content, compare);
        new BrickDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadMultipleParts() throws Exception {
        // 5L * 1024L * 1024L
        final BrickUploadFeature feature = new BrickUploadFeature(session, new BrickWriteFeature(session), 5 * 1024L * 1024L, 5);
        final Path root = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(root, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 5242881;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        feature.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledConnectionCallback());
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertTrue(new BrickFindFeature(session).find(test));
        assertEquals(content.length, new BrickAttributesFinderFeature(session).find(test).getSize());
        final byte[] compare = new byte[length];
        IOUtils.readFully(new BrickReadFeature(session).read(test, new TransferStatus().withLength(length), new DisabledConnectionCallback()), compare);
        assertArrayEquals(content, compare);
        new BrickDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
