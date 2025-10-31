package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MultipartUploadService;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class S3MultipartUploadServiceTest extends AbstractS3Test {

    @Test
    public void testUploadSinglePart() throws Exception {
        // 5L * 1024L * 1024L
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final CryptoUploadFeature<StorageObject> m = new CryptoUploadFeature<>(session,
                new S3MultipartUploadService(session, acl, 5L * 1024L * 1024L, 5),
                cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 5242880;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        m.upload(new CryptoWriteFeature<>(session, new S3WriteFeature(session, acl), cryptomator), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), count, writeStatus, null);
        assertEquals(content.length, count.getSent());
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(test));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().setLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUpload() throws Exception {
        // 5L * 1024L * 1024L
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final CryptoUploadFeature<StorageObject> m = new CryptoUploadFeature<>(session,
                new S3MultipartUploadService(session, acl, 5L * 1024L * 1024L, 5),
                cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        m.upload(new CryptoWriteFeature<>(session, new S3WriteFeature(session, acl), cryptomator), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(), writeStatus, null);
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(test));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().setLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadWithBulk() throws Exception {
        // 5L * 1024L * 1024L
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final TransferStatus writeStatus = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        writeStatus.setLength(content.length);
        final CryptoBulkFeature<Map<TransferItem, TransferStatus>> bulk = new CryptoBulkFeature<>(session, new DisabledBulkFeature(), cryptomator);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), writeStatus), new DisabledConnectionCallback());
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final CryptoUploadFeature<StorageObject> m = new CryptoUploadFeature<>(session,
                new S3MultipartUploadService(session, acl, 5L * 1024L * 1024L, 5),
                cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        IOUtils.write(content, local.getOutputStream(false));
        m.upload(new CryptoWriteFeature<>(session, new S3WriteFeature(session, acl), cryptomator), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(), writeStatus, null);
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new S3FindFeature(session, acl)).find(test));
        assertEquals(content.length, cryptomator.getFeature(session, AttributesFinder.class, new S3AttributesFinderFeature(session, acl)).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().setLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new S3ReadFeature(session), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new S3DefaultDeleteFeature(session, acl)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
