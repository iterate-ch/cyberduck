package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.CryptoListService;
import ch.cyberduck.core.cryptomator.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.CryptoUploadFeature;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class B2LargeUploadServiceTest {

    @Test
    public void testMultiplePartsWithSHA256Checksum() throws Exception {
        // 5L * 1024L * 1024L
        final Host host = new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                ));
        final B2Session session = new B2Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new Path("/test-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoUploadFeature m = new CryptoUploadFeature<>(session,
                new B2LargeUploadService(session, new B2WriteFeature(session), 5242880L, 5),
                new B2WriteFeature(session), cryptomator);
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = new byte[5242885];
        new Random().nextBytes(content);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        final Cryptor cryptor = cryptomator.getCryptor();
        final FileHeader header = cryptor.fileHeaderCryptor().create();
        status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
        status.setLength(content.length);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status, null);
        assertEquals((long) content.length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new CryptoFindFeature(session, new B2FindFeature(session), cryptomator).find(test));
        assertEquals(content.length, new CryptoListService(session, session, cryptomator).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final InputStream in = new CryptoReadFeature(session, new B2ReadFeature(session), cryptomator).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        new CryptoDeleteFeature(session, new B2DeleteFeature(session), cryptomator).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}