package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.test.IntegrationTest;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxWriteFeatureTest {

    @Test
    public void testWrite() throws Exception {
        final DropboxSession session = new DropboxSession(new Host(new DropboxProtocol(), new DropboxProtocol().getDefaultHostname()),
                new DisabledX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        if(user.equals("Dropbox OAuth2 Access Token")) {
                            return System.getProperties().getProperty("dropbox.accesstoken");
                        }
                        if(user.equals("Dropbox OAuth2 Refresh Token")) {
                            return System.getProperties().getProperty("dropbox.refreshtoken");
                        }
                        return null;
                    }
                }, new DisabledProgressListener())
                .connect(session, PathCache.empty(), new DisabledCancelCallback());
        final TransferStatus status = new TransferStatus();
        final byte[] content = new byte[1048576];
        new Random().nextBytes(content);
        status.setLength(content.length);
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final CryptoWriteFeature<String> writer = new CryptoWriteFeature<String>(session, new DropboxWriteFeature(session), cryptomator);
        final Cryptor cryptor = cryptomator.getCryptor();
        final FileHeader header = cryptor.fileHeaderCryptor().create();
        status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RotatingNonceGenerator(cryptomator.numberOfChunks(content.length)));
        status.setChecksum(writer.checksum().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new CryptoFindFeature(session, new DropboxFindFeature(session), cryptomator).find(test));
        assertEquals(content.length, new CryptoListService(session, session, cryptomator).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        assertEquals(content.length, new CryptoWriteFeature<>(session, new DropboxWriteFeature(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session), 150000000L), cryptomator).append(test, status.getLength(), PathCache.empty()).size, 0L);
        assertEquals(content.length, new CryptoWriteFeature<>(session, new DropboxWriteFeature(session, new DropboxFindFeature(session), new DropboxAttributesFinderFeature(session), 150000000L), cryptomator).append(test, status.getLength(), PathCache.empty()).size, 0L);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        final InputStream in = new CryptoReadFeature(session, new DropboxReadFeature(session), cryptomator).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(in, buffer);
        assertArrayEquals(content, buffer.toByteArray());
        new CryptoDeleteFeature(session, new DropboxDeleteFeature(session), cryptomator).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
