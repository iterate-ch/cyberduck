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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FindFeature;
import ch.cyberduck.core.b2.B2LargeUploadService;
import ch.cyberduck.core.b2.B2ReadFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
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
public class B2LargeUploadServiceTest extends AbstractB2Test {

    @Test
    public void testWrite() throws Exception {
        // 5L * 1024L * 1024L
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final CryptoUploadFeature m = new CryptoUploadFeature<>(session,
            new B2LargeUploadService(session, fileid, new B2WriteFeature(session, fileid), 5242880L, 5),
            new B2WriteFeature(session, fileid), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(5242885);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final FileHeader header = cryptomator.getFileHeaderCryptor().create();
        writeStatus.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final BytecountStreamListener counter = new BytecountStreamListener();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), counter, writeStatus, null);
        assertEquals(content.length, counter.getSent());
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new B2FindFeature(session, fileid)).find(test));
        assertEquals(content.length, new CryptoAttributesFeature(session, new B2AttributesFinderFeature(session, fileid), cryptomator).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().withLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session, fileid), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadWithBulk() throws Exception {
        // 5L * 1024L * 1024L
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final TransferStatus writeStatus = new TransferStatus();
        final int length = 5242885;
        final byte[] content = RandomUtils.nextBytes(length);
        writeStatus.setLength(content.length);
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final CryptoBulkFeature<Map<TransferItem, TransferStatus>> bulk = new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new B2DeleteFeature(session, fileid), cryptomator);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), writeStatus), new DisabledConnectionCallback());
        final CryptoUploadFeature m = new CryptoUploadFeature<>(session,
            new B2LargeUploadService(session, fileid, new B2WriteFeature(session, fileid), 5242880L, 5),
            new B2WriteFeature(session, fileid), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        IOUtils.write(content, local.getOutputStream(false));
        final BytecountStreamListener counter = new BytecountStreamListener();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), counter, writeStatus, null);
        assertEquals(content.length, counter.getSent());
        assertTrue(writeStatus.isComplete());
        assertTrue(cryptomator.getFeature(session, Find.class, new B2FindFeature(session, fileid)).find(test));
        assertEquals(content.length, new CryptoAttributesFeature(session, new B2AttributesFinderFeature(session, fileid), cryptomator).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().withLength(content.length);
        final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session, fileid), cryptomator).read(test, readStatus, new DisabledConnectionCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        cryptomator.getFeature(session, Delete.class, new B2DeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
