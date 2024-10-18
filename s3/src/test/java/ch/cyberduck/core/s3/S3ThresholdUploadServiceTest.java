package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3ThresholdUploadServiceTest extends AbstractS3Test {

    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3ThresholdUploadService m = new S3ThresholdUploadService(session, new S3AccessControlListFeature(session), 5 * 1024L);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TransferStatus status = new TransferStatus().withLength(5 * 1024L);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(), status, null);
    }

    @Test
    public void testUploadSinglePartEuCentral() throws Exception {
        final S3ThresholdUploadService service = new S3ThresholdUploadService(session, new S3AccessControlListFeature(session), 5 * 1024L);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1000);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        final BytecountStreamListener count = new BytecountStreamListener();
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), count, status, new DisabledLoginCallback());
        assertEquals(random.length, count.getSent(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test);
        assertEquals(random.length, attributes.getSize(), 0L);
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session, new S3AccessControlListFeature(session)).getClass(test));
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadSinglePartUsEast() throws Exception {
        final S3ThresholdUploadService service = new S3ThresholdUploadService(session, new S3AccessControlListFeature(session), 5 * 1024L);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1000);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        final BytecountStreamListener count = new BytecountStreamListener();
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), count, status, new DisabledLoginCallback());
        assertEquals(random.length, count.getSent());
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test);
        assertEquals(random.length, attributes.getSize());
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session, new S3AccessControlListFeature(session)).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(test);
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadZeroLength() throws Exception {
        final S3ThresholdUploadService service = new S3ThresholdUploadService(session, new S3AccessControlListFeature(session), 5 * 1024L);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(0);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        status.setMime("text/plain");
        final BytecountStreamListener count = new BytecountStreamListener();
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), count, status, new DisabledLoginCallback());
        assertEquals(random.length, count.getSent());
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test);
        assertEquals(random.length, attributes.getSize());
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
