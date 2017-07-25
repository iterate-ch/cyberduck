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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.b2.B2AttributesFinderFeature;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FindFeature;
import ch.cyberduck.core.b2.B2LargeUploadService;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.b2.B2ReadFeature;
import ch.cyberduck.core.b2.B2Session;
import ch.cyberduck.core.b2.B2WriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoUploadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadServiceTest {

    @Test
    public void testWrite() throws Exception {
        // 5L * 1024L * 1024L
        final Host host = new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                ));
        final B2Session session = new B2Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoUploadFeature m = new CryptoUploadFeature<>(session,
                new B2LargeUploadService(session, new B2WriteFeature(session), 5242880L, 5),
                new B2WriteFeature(session), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(5242885);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus writeStatus = new TransferStatus();
        final Cryptor cryptor = cryptomator.getCryptor();
        final FileHeader header = cryptor.fileHeaderCryptor().create();
        writeStatus.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
        writeStatus.setLength(content.length);
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), writeStatus, null);
        assertEquals((long) content.length, writeStatus.getOffset(), 0L);
        assertTrue(writeStatus.isComplete());
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session), cryptomator).find(test));
        assertEquals(content.length, new CryptoAttributesFeature(session, new B2AttributesFinderFeature(session), cryptomator).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().length(content.length);
        final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session), cryptomator).read(test, readStatus, new DisabledConnectionCallback(), new DisabledPasswordCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        new CryptoDeleteFeature(session, new B2DeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }

    @Test
    public void testUploadWithBulk() throws Exception {
        // 5L * 1024L * 1024L
        final Host host = new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                ));
        final B2Session session = new B2Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final TransferStatus writeStatus = new TransferStatus();
        final int length = 5242885;
        final byte[] content = RandomUtils.nextBytes(length);
        writeStatus.setLength(content.length);
        final CryptoBulkFeature<Void> bulk = new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new B2DeleteFeature(session), cryptomator);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, writeStatus), new DisabledConnectionCallback());
        final CryptoUploadFeature m = new CryptoUploadFeature<>(session,
                new B2LargeUploadService(session, new B2WriteFeature(session), 5242880L, 5),
                new B2WriteFeature(session), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        IOUtils.write(content, local.getOutputStream(false));
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), writeStatus, null);
        assertEquals((long) content.length, writeStatus.getOffset(), 0L);
        assertTrue(writeStatus.isComplete());
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session), cryptomator).find(test));
        assertEquals(content.length, new CryptoAttributesFeature(session, new B2AttributesFinderFeature(session), cryptomator).find(test).getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final TransferStatus readStatus = new TransferStatus().length(content.length);
        final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session), cryptomator).read(test, readStatus, new DisabledConnectionCallback(), new DisabledPasswordCallback());
        new StreamCopier(readStatus, readStatus).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        new CryptoDeleteFeature(session, new B2DeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}