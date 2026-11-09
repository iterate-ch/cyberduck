package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import com.google.api.services.storage.model.StorageObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DefaultUploadFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testUpload() throws Exception {
        final DefaultUploadFeature<StorageObject> m = new DefaultUploadFeature<>();
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = RandomUtils.nextBytes(1025);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        final StorageObject versionId = m.upload(new GoogleStorageWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                ProgressListener.noop, StreamListener.noop, status, LoginCallback.noop);
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        final PathAttributes attributes = new GoogleStorageListService(session).list(container, new DisabledListProgressListener()).find(new SimplePathPredicate(test)).attributes();
        assertEquals(random.length, attributes.getSize());
        assertEquals(String.valueOf(versionId.getGeneration()), attributes.getVersionId());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadVerifyChecksum() throws Exception {
        final Upload<StorageObject> upload = session.getFeature(Upload.class);
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = RandomUtils.nextBytes(1025);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        out.close();
        final TransferStatus status = new TransferStatus().setLength(random.length);
        assertTrue(upload.features(test, status).contains(Upload.Flags.checksumResponse));
        assertEquals(HashAlgorithm.md5, upload.checksum(test, status).getAlgorithm());
        // Calculated while streaming
        status.setVerify(upload.checksum(test, status).getAlgorithm());
        upload.upload(new GoogleStorageWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                ProgressListener.noop, StreamListener.noop, status, LoginCallback.noop);
        final Checksum expected = new MD5ChecksumCompute().compute(new ByteArrayInputStream(random), new TransferStatus());
        assertEquals(expected, status.getDigest());
        // Checksum returned by the server
        assertEquals(expected, status.getResponse().getChecksum());
        assertEquals(expected, new GoogleStorageAttributesFinderFeature(session).find(test).getChecksum());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadVerifyChecksumResume() throws Exception {
        final Upload<StorageObject> upload = session.getFeature(Upload.class);
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = RandomUtils.nextBytes(1025);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        out.close();
        // Content skipped is included in the checksum of the whole file
        final TransferStatus status = new TransferStatus().setOffset(100L).setLength(random.length - 100L)
                .setVerify(HashAlgorithm.md5);
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        new DefaultUploadFeature<StorageObject>().upload(new Write<StorageObject>() {
            @Override
            public StatusOutputStream<StorageObject> write(final Path file, final TransferStatus s, final ConnectionCallback callback) {
                return new StatusOutputStream<StorageObject>(new NullOutputStream()) {
                    @Override
                    public StorageObject getStatus() {
                        return new StorageObject();
                    }
                };
            }
        }, test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop, status, LoginCallback.noop);
        assertEquals(new Checksum(HashAlgorithm.md5, Hex.encodeHexString(digest.digest(random))), status.getDigest());
        local.delete();
    }
}
