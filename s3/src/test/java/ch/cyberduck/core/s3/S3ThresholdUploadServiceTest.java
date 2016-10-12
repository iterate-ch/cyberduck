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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jets3t.service.model.S3Object;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class S3ThresholdUploadServiceTest {

    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3ThresholdUploadService m = new S3ThresholdUploadService(session,
                new DisabledX509TrustManager(), new DefaultX509KeyManager(), 5 * 1024L);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TransferStatus status = new TransferStatus();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status, null);
    }

    @Test
    public void testUploadSinglePartEuCentral() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3ThresholdUploadService service = new S3ThresholdUploadService(session,
                new DisabledX509TrustManager(), new DefaultX509KeyManager(), 5 * 1024L);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final String random = RandomStringUtils.random(1000);
        IOUtils.write(random, local.getOutputStream(false), Charset.defaultCharset());
        final TransferStatus status = new TransferStatus();
        status.setLength((long) random.getBytes().length);
        status.setMime("text/plain");
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals((long) random.getBytes().length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(random.getBytes().length, attributes.getSize());
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }

    @Test
    public void testUploadSinglePartUsEast() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3ThresholdUploadService service = new S3ThresholdUploadService(session,
                new DisabledX509TrustManager(), new DefaultX509KeyManager(), 5 * 1024L);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final String random = RandomStringUtils.random(1000);
        IOUtils.write(random, local.getOutputStream(false), Charset.defaultCharset());
        final TransferStatus status = new TransferStatus();
        status.setLength((long) random.getBytes().length);
        status.setMime("text/plain");
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals((long) random.getBytes().length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(random.getBytes().length, attributes.getSize());
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}